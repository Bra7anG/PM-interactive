package com.pminteractive.pmtours.repository

import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.RegistroHitoGps
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.network.LinceApiService
import com.pminteractive.pmtours.network.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

interface ServicioRepository {
    fun obtenerTodosLosServicios(): Flow<List<Servicio>>
    fun obtenerServiciosPorTrabajador(trabajadorId: String): Flow<List<Servicio>>
    fun obtenerServiciosPorSede(driverIdsDeSede: List<String>): Flow<List<Servicio>>
    suspend fun obtenerServicioPorId(servicioId: String): Servicio?
    suspend fun responderAsignacion(servicioId: String, aceptar: Boolean): Result<Unit>
    suspend fun registrarCheckIn(servicioId: String, hito: RegistroHitoGps): Result<Unit>
    suspend fun registrarCheckOut(servicioId: String, hito: RegistroHitoGps): Result<Unit>
}

class HybridServicioRepository(
    private val apiService: LinceApiService,
    private val coordinadorRepository: CoordinadorRepository
) : ServicioRepository {

    private val _serviciosRemotos = MutableStateFlow<List<Servicio>>(emptyList())

    // Caché en memoria por código de trabajador (sobrevive a cortes de red en ruta)
    private val cachePorTrabajador = mutableMapOf<String, List<Servicio>>()

    override fun obtenerTodosLosServicios(): Flow<List<Servicio>> {
        return combinarConEstadoCompartido(_serviciosRemotos)
    }

    override fun obtenerServiciosPorTrabajador(trabajadorId: String): Flow<List<Servicio>> = flow {
        cargarServiciosRemotosDelTrabajador(trabajadorId)

        emitAll(
            combinarConEstadoCompartido(_serviciosRemotos) { servicio ->
                servicio.driverId.equals(trabajadorId, ignoreCase = true) ||
                        servicio.guiaId.equals(trabajadorId, ignoreCase = true)
            }
        )
    }

    override fun obtenerServiciosPorSede(driverIdsDeSede: List<String>): Flow<List<Servicio>> {
        return combinarConEstadoCompartido(_serviciosRemotos) { servicio ->
            servicio.driverId in driverIdsDeSede || servicio.driverId == null
        }
    }

    override suspend fun obtenerServicioPorId(servicioId: String): Servicio? {
        val locales = runCatching {
            coordinadorRepository.observarServiciosLocales().first()
        }.getOrDefault(emptyList())

        val todosEnMemoria = locales + _serviciosRemotos.value + cachePorTrabajador.values.flatten()
        val encontrado = todosEnMemoria.find { it.id == servicioId } ?: return null

        return aplicarOverride(encontrado, coordinadorRepository.observarCambiosEstado().value)
    }

    override suspend fun responderAsignacion(servicioId: String, aceptar: Boolean): Result<Unit> {
        val nuevoEstado = if (aceptar) EstadoServicio.CONFIRMADO else EstadoServicio.RECHAZADO
        coordinadorRepository.registrarCambioServicio(
            servicioId = servicioId,
            nuevoEstado = nuevoEstado
        )
        return Result.success(Unit)
    }

    override suspend fun registrarCheckIn(servicioId: String, hito: RegistroHitoGps): Result<Unit> {
        coordinadorRepository.registrarCambioServicio(
            servicioId = servicioId,
            nuevoEstado = EstadoServicio.EN_RUTA,
            checkIn = hito
        )
        return Result.success(Unit)
    }

    override suspend fun registrarCheckOut(servicioId: String, hito: RegistroHitoGps): Result<Unit> {
        coordinadorRepository.registrarCambioServicio(
            servicioId = servicioId,
            nuevoEstado = EstadoServicio.FINALIZADO,
            checkOut = hito
        )
        return Result.success(Unit)
    }

    private fun combinarConEstadoCompartido(
        flujoRemoto: Flow<List<Servicio>>,
        filtro: (Servicio) -> Boolean = { true }
    ): Flow<List<Servicio>> {
        return combine(
            flujoRemoto,
            coordinadorRepository.observarServiciosLocales(),
            coordinadorRepository.observarCambiosEstado()
        ) { remotos, locales, cambios ->
            (locales + remotos)
                .distinctBy { it.id }
                .filter(filtro)
                .map { aplicarOverride(it, cambios) }
                .sortedWith(compareBy({ it.fecha }, { it.horaInicio }))
        }
    }

    private fun aplicarOverride(
        servicio: Servicio,
        cambios: Map<String, ActualizacionServicioLocal>
    ): Servicio {
        val cambio = cambios[servicio.id] ?: return servicio
        return servicio.copy(
            estado = cambio.estado,
            checkIn = cambio.checkIn ?: servicio.checkIn,
            checkOut = cambio.checkOut ?: servicio.checkOut
        )
    }

    private suspend fun cargarServiciosRemotosDelTrabajador(trabajadorId: String) {
        val codigo = trabajadorId.trim().uppercase()

        val resultadoRemoto = runCatching {
            when {
                codigo.startsWith("TRA-") -> {
                    val idNumerico = apiService.getTransportistas(q = codigo, activo = null, pageSize = 20)
                        .data
                        .find { it.codigo.equals(codigo, ignoreCase = true) }
                        ?.id
                        ?: codigo.substringAfter("-").toIntOrNull()

                    if (idNumerico != null) {
                        apiService.getServicios(
                            transportistaId = idNumerico,
                            pageSize = 100
                        ).data.map { it.toDomain() }
                    } else {
                        emptyList()
                    }
                }

                codigo.startsWith("GUI-") -> {
                    val idNumerico = apiService.getGuias(q = codigo, activo = null, pageSize = 20)
                        .data
                        .find { it.codigo.equals(codigo, ignoreCase = true) }
                        ?.id
                        ?: codigo.substringAfter("-").toIntOrNull()

                    if (idNumerico != null) {
                        apiService.getServicios(
                            guiaId = idNumerico,
                            pageSize = 100
                        ).data.map { it.toDomain() }
                    } else {
                        emptyList()
                    }
                }

                else -> emptyList()
            }
        }

        resultadoRemoto.onSuccess { listaApi ->
            // Guarda en caché de sesión y actualiza el flujo activo
            cachePorTrabajador[codigo] = listaApi
            _serviciosRemotos.value = listaApi
        }.onFailure {
            // Sin internet o API apagada: conserva la última caché en memoria o usa respaldo offline
            val enCache = cachePorTrabajador[codigo]
            _serviciosRemotos.value = enCache ?: generarRespaldoOffline(codigo)
        }
    }

    private fun generarRespaldoOffline(codigo: String): List<Servicio> {
        val hoy = LocalDate.now().toString()
        val manana = LocalDate.now().plusDays(1).toString()

        return if (codigo.startsWith("GUI-")) {
            listOf(
                Servicio(
                    id = "SRV-OFF-201",
                    numeroReserva = "WHFT-OFF-01",
                    titulo = "Servicio Guiado: City Tour Histórico (Respaldo Offline)",
                    tipo = TipoServicio.GUIDED_SERVICE,
                    fecha = hoy,
                    horaInicio = "09:30",
                    horaFinEstimada = "13:00",
                    lugarPickup = "Hotel Barrio Lastarria, Santiago",
                    lugarDropOff = "Plaza de Armas, Santiago",
                    cantidadPasajeros = 4,
                    paxTexto = "4+0",
                    driverId = null,
                    vehiculoId = null,
                    guiaId = codigo,
                    estado = EstadoServicio.CONFIRMADO,
                    tarifaAcordada = 60000.0,
                    notasAgencia = "Modo respaldo local activo por falta de conexión con servidor.",
                    detalleItinerario = "Recorrido patrimonial centro histórico."
                )
            )
        } else {
            listOf(
                Servicio(
                    id = "SRV-OFF-101",
                    numeroReserva = "WHFT-OFF-02",
                    titulo = "Transfer In: Aeropuerto SCL -> Hotel Providencia (Respaldo Offline)",
                    tipo = TipoServicio.TRANSFER,
                    fecha = hoy,
                    horaInicio = "10:00",
                    horaFinEstimada = "11:30",
                    lugarPickup = "Aeropuerto Internacional Arturo Merino Benítez",
                    lugarDropOff = "Hotel Sheraton, Providencia",
                    cantidadPasajeros = 2,
                    paxTexto = "2+0",
                    driverId = codigo,
                    vehiculoId = "VEH-1",
                    guiaId = null,
                    estado = EstadoServicio.ASIGNADO,
                    tarifaAcordada = 45000.0,
                    notasAgencia = "Modo respaldo local activo por falta de conexión con servidor.",
                    detalleItinerario = "Traslado directo por Costanera Norte."
                ),
                Servicio(
                    id = "SRV-OFF-102",
                    numeroReserva = "WHFT-OFF-03",
                    titulo = "Excursión Viña Concha y Toro (Respaldo Offline)",
                    tipo = TipoServicio.EXCURSION,
                    fecha = manana,
                    horaInicio = "09:00",
                    horaFinEstimada = "14:30",
                    lugarPickup = "Hotel Cumbres Lastarria",
                    lugarDropOff = "Hotel Cumbres Lastarria",
                    cantidadPasajeros = 3,
                    paxTexto = "3+0",
                    driverId = codigo,
                    vehiculoId = "VEH-1",
                    guiaId = null,
                    estado = EstadoServicio.CONFIRMADO,
                    tarifaAcordada = 80000.0,
                    notasAgencia = "Degustación incluida.",
                    detalleItinerario = "Salida hacia Pirque y retorno."
                )
            )
        }
    }
}
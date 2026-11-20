package com.pminteractive.pmtours.repository

import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.RegistroHitoGps
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.network.LinceApiService
import com.pminteractive.pmtours.network.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

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

    // Caché en memoria de los servicios descargados desde la API para el operador activo
    private val _serviciosRemotos = MutableStateFlow<List<Servicio>>(emptyList())

    override fun obtenerTodosLosServicios(): Flow<List<Servicio>> {
        return combinarConEstadoCompartido(_serviciosRemotos)
    }

    override fun obtenerServiciosPorTrabajador(trabajadorId: String): Flow<List<Servicio>> = flow {
        // 1. Descarga desde la API los servicios reales del trabajador (si existe en Southbound)
        cargarServiciosRemotosDelTrabajador(trabajadorId)

        // 2. Emite de forma reactiva la combinación de:
        //    servicios de API + servicios creados por el Coordinador + cambios de estado en vivo
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

        val encontrado = (locales + _serviciosRemotos.value).find { it.id == servicioId }
            ?: return null

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
            val unificados = (locales + remotos)
                .distinctBy { it.id }
                .filter(filtro)
                .map { aplicarOverride(it, cambios) }
                .sortedWith(compareBy({ it.fecha }, { it.horaInicio }))

            unificados
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

        runCatching {
            when {
                codigo.startsWith("TRA-") -> {
                    // Busca el ID numérico real del transportista (ej. "TRA-021" -> 21)
                    val idNumerico = apiService.getTransportistas(q = codigo, activo = null, pageSize = 20)
                        .data
                        .find { it.codigo.equals(codigo, ignoreCase = true) }
                        ?.id
                        ?: codigo.substringAfter("-").toIntOrNull()

                    if (idNumerico != null) {
                        val lista = apiService.getServicios(
                            transportistaId = idNumerico,
                            pageSize = 100
                        ).data.map { it.toDomain() }
                        _serviciosRemotos.value = lista
                    }
                }

                codigo.startsWith("GUI-") -> {
                    // Busca el ID numérico real del guía (ej. "GUI-012" -> 12)
                    val idNumerico = apiService.getGuias(q = codigo, activo = null, pageSize = 20)
                        .data
                        .find { it.codigo.equals(codigo, ignoreCase = true) }
                        ?.id
                        ?: codigo.substringAfter("-").toIntOrNull()

                    if (idNumerico != null) {
                        val lista = apiService.getServicios(
                            guiaId = idNumerico,
                            pageSize = 100
                        ).data.map { it.toDomain() }
                        _serviciosRemotos.value = lista
                    }
                }
            }
        }
    }
}
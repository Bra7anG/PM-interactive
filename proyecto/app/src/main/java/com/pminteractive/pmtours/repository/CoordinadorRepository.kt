package com.pminteractive.pmtours.repository

import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.RegistroHitoGps
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.network.LinceApiService
import com.pminteractive.pmtours.network.dto.*
import com.pminteractive.pmtours.network.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.File
import java.time.LocalDate

data class ActualizacionServicioLocal(
    val estado: EstadoServicio,
    val checkIn: RegistroHitoGps? = null,
    val checkOut: RegistroHitoGps? = null
)

typealias MapaCambiosServicio = Map<String, ActualizacionServicioLocal>

interface CoordinadorRepository {
    suspend fun obtenerCatalogos(): Result<CatalogosDto>
    suspend fun obtenerResumen(fecha: String, oficinaCodigo: String): Result<ResumenDto>
    suspend fun obtenerServiciosPorDiaYSede(fecha: String, oficinaCodigo: String): Result<List<Servicio>>
    suspend fun obtenerTransportistas(oficinaCodigo: String? = null, query: String? = null): Result<List<TransportistaDto>>
    suspend fun obtenerGuias(oficinaCodigo: String? = null, query: String? = null): Result<List<GuiaDto>>
    suspend fun obtenerClientes(query: String? = null): Result<List<ClienteDto>>
    suspend fun crearServicioLocal(oficinaCodigo: String, servicio: Servicio): Result<Unit>

    // Puente compartido con el rol Driver / Guía
    fun observarServiciosLocales(): Flow<List<Servicio>>
    fun observarCambiosEstado(): StateFlow<MapaCambiosServicio>
    fun registrarCambioServicio(
        servicioId: String,
        nuevoEstado: EstadoServicio,
        checkIn: RegistroHitoGps? = null,
        checkOut: RegistroHitoGps? = null
    )
}

class HybridCoordinadorRepository(
    private val apiService: LinceApiService
) : CoordinadorRepository {

    private data class ServicioLocal(
        val oficinaCodigo: String,
        val servicio: Servicio
    )

    private val _serviciosLocales = MutableStateFlow<List<ServicioLocal>>(emptyList())
    private val _cambiosEstado = MutableStateFlow<MapaCambiosServicio>(emptyMap())

    // 1. Apuntamos a archivos seguros dentro del teléfono
    private val archivoServicios = File(AppContainer.filesDir, "servicios_locales.json")
    private val archivoEstados = File(AppContainer.filesDir, "cambios_estado.json")

    // Cachés en memoria para resiliencia offline
    private val cacheServiciosDia = mutableMapOf<String, List<Servicio>>()
    private val cacheTransportistas = mutableMapOf<String, List<TransportistaDto>>()
    private val cacheGuias = mutableMapOf<String, List<GuiaDto>>()
    private var cacheClientes: List<ClienteDto>? = null
    private var cacheCatalogos: CatalogosDto? = null

    init {
        cargarDatosLocales()
    }

    // 2. Leemos los JSON al abrir la app
    private fun cargarDatosLocales() {
        runCatching {
            if (archivoServicios.exists()) {
                val tipoLista = object : com.google.gson.reflect.TypeToken<List<ServicioLocal>>() {}.type
                val lista: List<ServicioLocal> = AppContainer.gson.fromJson(archivoServicios.readText(), tipoLista)
                _serviciosLocales.value = lista
            }
            if (archivoEstados.exists()) {
                val tipoMapa = object : com.google.gson.reflect.TypeToken<MapaCambiosServicio>() {}.type
                val mapa: MapaCambiosServicio = AppContainer.gson.fromJson(archivoEstados.readText(), tipoMapa)
                _cambiosEstado.value = mapa
            }
        }
    }

    // 3. Escribimos en el disco al registrar cambios
    private fun guardarDatosLocales() {
        runCatching {
            archivoServicios.writeText(AppContainer.gson.toJson(_serviciosLocales.value))
            archivoEstados.writeText(AppContainer.gson.toJson(_cambiosEstado.value))
        }
    }

    override fun observarServiciosLocales(): Flow<List<Servicio>> {
        return _serviciosLocales.map { lista -> lista.map { it.servicio } }
    }

    override fun observarCambiosEstado(): StateFlow<MapaCambiosServicio> {
        return _cambiosEstado.asStateFlow()
    }

    override fun registrarCambioServicio(
        servicioId: String,
        nuevoEstado: EstadoServicio,
        checkIn: RegistroHitoGps?,
        checkOut: RegistroHitoGps?
    ) {
        _cambiosEstado.update { mapaActual ->
            val previo = mapaActual[servicioId]
            mapaActual + (
                    servicioId to ActualizacionServicioLocal(
                        estado = nuevoEstado,
                        checkIn = checkIn ?: previo?.checkIn,
                        checkOut = checkOut ?: previo?.checkOut
                    )
                    )
        }
        guardarDatosLocales()
    }

    private fun aplicarOverride(servicio: Servicio): Servicio {
        val cambio = _cambiosEstado.value[servicio.id] ?: return servicio
        return servicio.copy(
            estado = cambio.estado,
            checkIn = cambio.checkIn ?: servicio.checkIn,
            checkOut = cambio.checkOut ?: servicio.checkOut
        )
    }

    override suspend fun obtenerCatalogos(): Result<CatalogosDto> {
        return runCatching {
            apiService.getCatalogos().data.also { cacheCatalogos = it }
        }.recoverCatching {
            cacheCatalogos ?: CatalogosDto(
                oficinas = listOf(
                    OficinaDto("SCL", "Santiago"),
                    OficinaDto("PUX", "Puerto Varas"),
                    OficinaDto("PUQ", "Punta Arenas")
                ),
                idiomas = listOf(
                    IdiomaDto("ES", "Español"),
                    IdiomaDto("EN", "Inglés"),
                    IdiomaDto("DE", "Alemán")
                ),
                tiposServicio = emptyList(),
                estadosServicio = emptyList(),
                statusPL = emptyList(),
                tiposVehiculo = emptyList()
            )
        }
    }

    override suspend fun obtenerResumen(fecha: String, oficinaCodigo: String): Result<ResumenDto> = runCatching {
        apiService.getResumen(
            fechaDesde = fecha,
            fechaHasta = fecha,
            oficina = oficinaCodigo
        ).data
    }

    override suspend fun obtenerServiciosPorDiaYSede(
        fecha: String,
        oficinaCodigo: String
    ): Result<List<Servicio>> = runCatching {
        val claveCache = "$oficinaCodigo-$fecha"

        val remotos = runCatching {
            apiService.getServicios(
                fechaDesde = fecha,
                fechaHasta = fecha,
                oficina = oficinaCodigo,
                pageSize = 100
            ).data.map { it.toDomain() }.also { cacheServiciosDia[claveCache] = it }
        }.getOrElse {
            cacheServiciosDia[claveCache] ?: respaldoServiciosCoordinador(fecha, oficinaCodigo)
        }.map { aplicarOverride(it) }

        // AQUI CAMBIAMOS EL FILTRADO PARA IGNORAR ESPACIOS O MAYUSCULAS
        val locales = _serviciosLocales.value
            .filter { it.oficinaCodigo.trim().equals(oficinaCodigo.trim(), ignoreCase = true) &&
                    it.servicio.fecha.trim() == fecha.trim() }
            .map { aplicarOverride(it.servicio) }

        locales + remotos
    }

    override suspend fun obtenerTransportistas(
        oficinaCodigo: String?,
        query: String?
    ): Result<List<TransportistaDto>> = runCatching {
        val clave = "${oficinaCodigo ?: "ALL"}-${query.orEmpty()}"
        runCatching {
            apiService.getTransportistas(
                oficina = oficinaCodigo,
                activo = true,
                q = query?.trim()?.takeIf { it.isNotEmpty() },
                pageSize = 100
            ).data.also { cacheTransportistas[clave] = it }
        }.getOrElse {
            cacheTransportistas[clave] ?: respaldoTransportistas()
        }
    }

    override suspend fun obtenerGuias(
        oficinaCodigo: String?,
        query: String?
    ): Result<List<GuiaDto>> = runCatching {
        val clave = "${oficinaCodigo ?: "ALL"}-${query.orEmpty()}"
        runCatching {
            apiService.getGuias(
                oficina = oficinaCodigo,
                activo = true,
                q = query?.trim()?.takeIf { it.isNotEmpty() },
                pageSize = 100
            ).data.also { cacheGuias[clave] = it }
        }.getOrElse {
            cacheGuias[clave] ?: respaldoGuias()
        }
    }

    override suspend fun obtenerClientes(query: String?): Result<List<ClienteDto>> = runCatching {
        runCatching {
            apiService.getClientes(
                q = query?.trim()?.takeIf { it.isNotEmpty() },
                pageSize = 100
            ).data.also { if (query.isNullOrBlank()) cacheClientes = it }
        }.getOrElse {
            cacheClientes ?: listOf(
                ClienteDto(1, "CLI-001", "Southbound Demo Agency", "Chile")
            )
        }
    }

    override suspend fun crearServicioLocal(
        oficinaCodigo: String,
        servicio: Servicio
    ): Result<Unit> = runCatching {
        _serviciosLocales.update { listaActual ->
            listOf(ServicioLocal(oficinaCodigo, servicio)) + listaActual
        }
        guardarDatosLocales()
    }

    // Datos mínimos de respaldo
    private fun respaldoServiciosCoordinador(fecha: String, oficinaCodigo: String): List<Servicio> {
        if (fecha != LocalDate.now().toString()) return emptyList()
        return listOf(
            Servicio(
                id = "SRV-BACKUP-01",
                numeroReserva = "WHFT-BKP-01",
                titulo = "Transfer In: Aeropuerto -> Hotel ($oficinaCodigo · Respaldo)",
                tipo = TipoServicio.TRANSFER,
                fecha = fecha,
                horaInicio = "09:00",
                horaFinEstimada = "10:30",
                lugarPickup = "Aeropuerto Principal",
                lugarDropOff = "Hotel Centro",
                cantidadPasajeros = 2,
                paxTexto = "2+0",
                driverId = "TRA-001",
                vehiculoId = "VEH-1",
                guiaId = null,
                estado = EstadoServicio.CONFIRMADO,
                tarifaAcordada = 45000.0,
                notasAgencia = "Servicio de respaldo offline.",
                detalleItinerario = "Traslado estándar."
            )
        )
    }

    private fun respaldoTransportistas(): List<TransportistaDto> = listOf(
        TransportistaDto(
            id = 1,
            codigo = "TRA-001",
            nombre = "Catalina Ibáñez Campos",
            oficina = OficinaDto("SCL", "Santiago"),
            vehiculos = listOf(VehiculoDto(1, "ZZ-1001", "SEDAN", 3)),
            evaluacionPromedio = 4.43,
            fechaIngreso = "2020-04-26",
            activo = true
        )
    )

    private fun respaldoGuias(): List<GuiaDto> = listOf(
        GuiaDto(
            id = 12,
            codigo = "GUI-012",
            nombre = "Andrés Godoy Peña",
            oficina = OficinaDto("SCL", "Santiago"),
            idiomas = listOf(IdiomaDto("ES", "Español"), IdiomaDto("EN", "Inglés")),
            evaluacionPromedio = 3.64,
            fechaIngreso = "2017-12-05",
            activo = true
        )
    )
}
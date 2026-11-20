package com.pminteractive.pmtours.repository

import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.RegistroHitoGps
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.network.LinceApiService
import com.pminteractive.pmtours.network.dto.*
import com.pminteractive.pmtours.network.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

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
    }

    private fun aplicarOverride(servicio: Servicio): Servicio {
        val cambio = _cambiosEstado.value[servicio.id] ?: return servicio
        return servicio.copy(
            estado = cambio.estado,
            checkIn = cambio.checkIn ?: servicio.checkIn,
            checkOut = cambio.checkOut ?: servicio.checkOut
        )
    }

    override suspend fun obtenerCatalogos(): Result<CatalogosDto> = runCatching {
        apiService.getCatalogos().data
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
        val remotos = apiService.getServicios(
            fechaDesde = fecha,
            fechaHasta = fecha,
            oficina = oficinaCodigo,
            pageSize = 100
        ).data.map { aplicarOverride(it.toDomain()) }

        val locales = _serviciosLocales.value
            .filter { it.oficinaCodigo == oficinaCodigo && it.servicio.fecha == fecha }
            .map { aplicarOverride(it.servicio) }

        locales + remotos
    }

    override suspend fun obtenerTransportistas(
        oficinaCodigo: String?,
        query: String?
    ): Result<List<TransportistaDto>> = runCatching {
        apiService.getTransportistas(
            oficina = oficinaCodigo,
            activo = true,
            q = query?.trim()?.takeIf { it.isNotEmpty() },
            pageSize = 100
        ).data
    }

    override suspend fun obtenerGuias(
        oficinaCodigo: String?,
        query: String?
    ): Result<List<GuiaDto>> = runCatching {
        apiService.getGuias(
            oficina = oficinaCodigo,
            activo = true,
            q = query?.trim()?.takeIf { it.isNotEmpty() },
            pageSize = 100
        ).data
    }

    override suspend fun obtenerClientes(query: String?): Result<List<ClienteDto>> = runCatching {
        apiService.getClientes(
            q = query?.trim()?.takeIf { it.isNotEmpty() },
            pageSize = 100
        ).data
    }

    override suspend fun crearServicioLocal(
        oficinaCodigo: String,
        servicio: Servicio
    ): Result<Unit> = runCatching {
        _serviciosLocales.update { listaActual ->
            listOf(ServicioLocal(oficinaCodigo, servicio)) + listaActual
        }
    }
}
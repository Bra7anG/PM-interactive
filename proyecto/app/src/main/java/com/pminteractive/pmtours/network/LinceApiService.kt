package com.pminteractive.pmtours.network

import com.pminteractive.pmtours.network.dto.*
import retrofit2.http.GET
import retrofit2.http.Query

interface LinceApiService {

    // 0. Estado de salud del servidor y base de datos
    @GET("health")
    suspend fun getHealth(): HealthDto

    // 1. Catálogos maestros (oficinas, idiomas, tipos de servicio y estados)
    @GET("api/v1/catalogos")
    suspend fun getCatalogos(): ApiResponse<CatalogosDto>

    // 2. Totales agregados para los KPIs del dashboard
    @GET("api/v1/resumen")
    suspend fun getResumen(
        @Query("fechaDesde") fechaDesde: String? = null,
        @Query("fechaHasta") fechaHasta: String? = null,
        @Query("oficina") oficina: String? = null
    ): ApiResponse<ResumenDto>

    // 3. Listado de servicios con filtros combinables
    @GET("api/v1/servicios")
    suspend fun getServicios(
        @Query("fechaDesde") fechaDesde: String? = null,
        @Query("fechaHasta") fechaHasta: String? = null,
        @Query("oficina") oficina: String? = null,
        @Query("tipo") tipo: String? = null,
        @Query("estadoServicio") estadoServicio: String? = null,
        @Query("statusPL") statusPL: String? = null,
        @Query("guiaId") guiaId: Int? = null,
        @Query("transportistaId") transportistaId: Int? = null,
        @Query("q") q: String? = null,
        @Query("orden") orden: String? = "fecha",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): PaginatedResponse<ServicioDto>

    // 4. Transportistas y su flota de vehículos
    @GET("api/v1/transportistas")
    suspend fun getTransportistas(
        @Query("oficina") oficina: String? = null,
        @Query("activo") activo: Boolean? = true,
        @Query("capacidadMin") capacidadMin: Int? = null,
        @Query("q") q: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): PaginatedResponse<TransportistaDto>

    // 5. Guías turísticos y sus idiomas
    @GET("api/v1/guias")
    suspend fun getGuias(
        @Query("oficina") oficina: String? = null,
        @Query("idioma") idioma: String? = null,
        @Query("activo") activo: Boolean? = true,
        @Query("q") q: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): PaginatedResponse<GuiaDto>

    // 6. Agencias / clientes que originan las reservas
    @GET("api/v1/clientes")
    suspend fun getClientes(
        @Query("pais") pais: String? = null,
        @Query("q") q: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): PaginatedResponse<ClienteDto>
}
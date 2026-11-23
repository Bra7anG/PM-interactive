package com.pminteractive.pmtours.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface BackendApiService {

    @PATCH("api/v1/servicios/{id}/respuesta")
    suspend fun responderAsignacion(
        @Path("id") id: String,
        @Body request: BackendRespuestaRequest
    ): Response<BackendServicioResponse>

    @POST("api/v1/servicios/{id}/check-in")
    suspend fun registrarCheckIn(
        @Path("id") id: String,
        @Body request: BackendHitoRequest
    ): Response<BackendServicioResponse>

    @POST("api/v1/servicios/{id}/check-out")
    suspend fun registrarCheckOut(
        @Path("id") id: String,
        @Body request: BackendHitoRequest
    ): Response<BackendServicioResponse>
}

data class BackendRespuestaRequest(val aceptar: Boolean)

data class BackendHitoRequest(
    val latitud: Double,
    val longitud: Double,
    val direccionReferencia: String
)

data class BackendServicioResponse(
    val id: String,
    val estado: String,
    val fecha: String,
    val horaInicio: String,
    val checkIn: BackendHitoResponse?,
    val checkOut: BackendHitoResponse?
)

data class BackendHitoResponse(
    val latitud: Double,
    val longitud: Double,
    val direccionReferencia: String,
    val timestamp: Long,
    val aTiempo: Boolean
)

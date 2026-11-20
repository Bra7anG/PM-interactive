package com.pminteractive.pmtours.repository

import com.pminteractive.pmtours.network.LinceApiService
import com.pminteractive.pmtours.network.RetrofitClient

object AppContainer {
    val apiService: LinceApiService by lazy { RetrofitClient.apiService }
    val authRepository: AuthRepository by lazy { HybridAuthRepository(apiService) }
    val coordinadorRepository: CoordinadorRepository by lazy { HybridCoordinadorRepository(apiService) }
    val servicioRepository: ServicioRepository by lazy {
        HybridServicioRepository(apiService, coordinadorRepository)
    }
    val disponibilidadRepository: DisponibilidadRepository by lazy { FakeDisponibilidadRepository() }
}
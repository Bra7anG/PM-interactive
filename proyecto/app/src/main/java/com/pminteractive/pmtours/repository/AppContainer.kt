package com.pminteractive.pmtours.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.pminteractive.pmtours.network.BackendApiClient
import com.pminteractive.pmtours.network.LinceApiService
import com.pminteractive.pmtours.network.RetrofitClient

object AppContainer {
    private lateinit var appContext: Context

    fun inicializar(context: Context) {
        if (!this::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }

    val sharedPreferences: SharedPreferences by lazy {
        if (this::appContext.isInitialized) {
            appContext.getSharedPreferences("lince_session", Context.MODE_PRIVATE)
        } else {
            com.pminteractive.pmtours.support.InMemorySharedPreferences()
        }
    }

    val gson: Gson by lazy { Gson() }

    val filesDir by lazy { appContext.filesDir }

    val apiService: LinceApiService by lazy { RetrofitClient.apiService }
    val authRepository: AuthRepository by lazy { HybridAuthRepository(apiService) }
    val coordinadorRepository: CoordinadorRepository by lazy { HybridCoordinadorRepository(apiService) }
    val servicioRepository: ServicioRepository by lazy {
        HybridServicioRepository(apiService, coordinadorRepository, BackendApiClient.apiService)
    }
    val disponibilidadRepository: DisponibilidadRepository by lazy { FakeDisponibilidadRepository() }
}
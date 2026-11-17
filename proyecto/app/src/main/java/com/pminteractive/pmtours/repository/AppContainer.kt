package com.pminteractive.pmtours.repository

object AppContainer {
    val authRepository: AuthRepository by lazy { FakeAuthRepository() }
    val servicioRepository: ServicioRepository by lazy { FakeServicioRepository() }
    val disponibilidadRepository: DisponibilidadRepository by lazy { FakeDisponibilidadRepository() }
}

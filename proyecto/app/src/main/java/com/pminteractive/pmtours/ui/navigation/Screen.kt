package com.pminteractive.pmtours.ui.navigation

sealed class Screen(val route: String) {
    // Autenticación
    data object Login : Screen("login")

    // Pantallas principales del Operador (Driver / Guía)
    data object Servicios : Screen("operador_servicios")
    data object Calendario : Screen("operador_calendario")
    data object Perfil : Screen("operador_perfil")

    // Detalle de un viaje específico
    data object DetalleServicio : Screen("detalle_servicio/{servicioId}") {
        fun crearRuta(servicioId: String) = "detalle_servicio/$servicioId"
    }
}
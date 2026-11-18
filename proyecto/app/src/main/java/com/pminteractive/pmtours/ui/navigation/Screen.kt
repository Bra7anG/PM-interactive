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

    // Panel del Coordinador
    data object PanelCoordinador : Screen("coordinador_panel")
    data object PerfilCoordinador : Screen("coordinador_perfil")

    // Panel del Administrador
    data object PanelAdministrador : Screen("administrador_panel")
    data object PerfilAdministrador : Screen("administrador_perfil")
}
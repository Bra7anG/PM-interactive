package com.pminteractive.pmtours.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.ui.screens.administrador.PanelAdministradorScreen
import com.pminteractive.pmtours.ui.screens.administrador.PerfilAdministradorScreen
import com.pminteractive.pmtours.ui.screens.auth.LoginScreen
import com.pminteractive.pmtours.ui.screens.coordinador.PanelCoordinadorScreen
import com.pminteractive.pmtours.ui.screens.coordinador.PerfilCoordinadorScreen
import com.pminteractive.pmtours.ui.screens.driverGuia.CalendarioScreen
import com.pminteractive.pmtours.ui.screens.driverGuia.DetalleServicioScreen
import com.pminteractive.pmtours.ui.screens.driverGuia.PerfilScreen
import com.pminteractive.pmtours.ui.screens.driverGuia.ServiciosScreen
import com.pminteractive.pmtours.viewmodel.ServicioViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    var usuarioSesion by remember {
        mutableStateOf(AppContainer.authRepository.obtenerUsuarioActual())
    }
    val servicioViewModel: ServicioViewModel = viewModel()

    LaunchedEffect(usuarioSesion?.id) {
        usuarioSesion?.id?.let { servicioViewModel.cargarServiciosDelUsuario(it) }
    }

    val primerNombre = remember(usuarioSesion) {
        usuarioSesion?.nombreCompleto?.substringBefore(" ") ?: "Operador"
    }

    val cambiarTab: (String) -> Unit = { rutaDestino ->
        if (navController.currentDestination?.route != rutaDestino) {
            navController.navigate(rutaDestino) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val cerrarSesion: () -> Unit = {
        val rutaPanel = when (usuarioSesion?.rol) {
            RolUsuario.COORDINADOR -> Screen.PanelCoordinador.route
            RolUsuario.ADMINISTRADOR -> Screen.PanelAdministrador.route
            else -> Screen.Servicios.route
        }
        AppContainer.authRepository.cerrarSesion()
        servicioViewModel.limpiar()
        usuarioSesion = null
        navController.navigate(Screen.Login.route) {
            popUpTo(rutaPanel) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // 1. Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginExitoso = { usuarioLogueado ->
                    usuarioSesion = usuarioLogueado
                    val rutaPrincipal = when (usuarioLogueado.rol) {
                        RolUsuario.COORDINADOR -> Screen.PanelCoordinador.route
                        RolUsuario.ADMINISTRADOR -> Screen.PanelAdministrador.route
                        else -> Screen.Servicios.route
                    }
                    navController.navigate(rutaPrincipal) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Tab Servicios
        composable(Screen.Servicios.route) {
            ServiciosScreen(
                usuarioId = usuarioSesion?.id ?: "TRA-001",
                nombreUsuario = primerNombre,
                onServicioClick = { servicio ->
                    // Navega pasando el ID del viaje seleccionado
                    navController.navigate(Screen.DetalleServicio.crearRuta(servicio.id))
                },
                onCambiarTab = cambiarTab,
                viewModel = servicioViewModel
            )
        }

        // 3. Detalle del Servicio (Ruta con parámetro servicioId)
        composable(
            route = Screen.DetalleServicio.route,
            arguments = listOf(navArgument("servicioId") { type = NavType.StringType })
        ) { backStackEntry ->
            val servicioId = backStackEntry.arguments?.getString("servicioId") ?: ""
            DetalleServicioScreen(
                servicioId = servicioId,
                usuarioId = usuarioSesion?.id ?: "TRA-001",
                onVolver = { navController.popBackStack() },
                viewModel = servicioViewModel
            )
        }

        // 3. Tab Calendario
        composable(Screen.Calendario.route) {
            CalendarioScreen(
                usuario = usuarioSesion,
                onCambiarTab = cambiarTab,
                servicioViewModel = servicioViewModel
            )
        }

        // 5. Tab Perfil
        composable(Screen.Perfil.route) {
            PerfilScreen(
                usuario = usuarioSesion,
                onCambiarTab = cambiarTab,
                onCerrarSesion = cerrarSesion,
                servicioViewModel = servicioViewModel
            )
        }

        // 6. Panel Coordinador
        composable(Screen.PanelCoordinador.route) {
            PanelCoordinadorScreen(
                usuario = usuarioSesion,
                onCambiarTab = cambiarTab,
                onCerrarSesion = cerrarSesion
            )
        }

        composable(Screen.PerfilCoordinador.route) {
            PerfilCoordinadorScreen(
                usuario = usuarioSesion,
                onCambiarTab = cambiarTab,
                onCerrarSesion = cerrarSesion
            )
        }

        // 7. Panel Administrador
        composable(Screen.PanelAdministrador.route) {
            PanelAdministradorScreen(
                usuario = usuarioSesion,
                onCambiarTab = cambiarTab,
                onCerrarSesion = cerrarSesion
            )
        }

        composable(Screen.PerfilAdministrador.route) {
            PerfilAdministradorScreen(
                usuario = usuarioSesion,
                onCambiarTab = cambiarTab,
                onCerrarSesion = cerrarSesion
            )
        }
    }
}
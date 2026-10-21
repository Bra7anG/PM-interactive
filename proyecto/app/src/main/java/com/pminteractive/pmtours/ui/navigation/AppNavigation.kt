package com.pminteractive.pmtours.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.ui.screens.auth.LoginScreen
import com.pminteractive.pmtours.ui.screens.driverGuia.CalendarioScreen
import com.pminteractive.pmtours.ui.screens.driverGuia.DetalleServicioScreen
import com.pminteractive.pmtours.ui.screens.driverGuia.PerfilScreen
import com.pminteractive.pmtours.ui.screens.driverGuia.ServiciosScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    var usuarioSesion by remember { mutableStateOf<Usuario?>(null) }

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

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // 1. Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginExitoso = { usuarioLogueado ->
                    usuarioSesion = usuarioLogueado
                    navController.navigate(Screen.Servicios.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Tab Servicios
        composable(Screen.Servicios.route) {
            ServiciosScreen(
                nombreUsuario = primerNombre,
                onServicioClick = { servicio ->
                    // Navega pasando el ID del viaje seleccionado
                    navController.navigate(Screen.DetalleServicio.crearRuta(servicio.id))
                },
                onCambiarTab = cambiarTab
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
                onVolver = { navController.popBackStack() }
            )
        }

        // 3. Tab Calendario
        composable(Screen.Calendario.route) {
            CalendarioScreen(
                usuario = usuarioSesion,
                onCambiarTab = cambiarTab
            )
        }

        // 5. Tab Perfil
        composable(Screen.Perfil.route) {
            PerfilScreen(
                usuario = usuarioSesion, // Pasa el usuario autenticado real
                onCambiarTab = cambiarTab,
                onCerrarSesion = {
                    usuarioSesion = null
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Servicios.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
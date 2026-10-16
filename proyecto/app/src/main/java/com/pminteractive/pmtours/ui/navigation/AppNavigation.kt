package com.pminteractive.pmtours.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.ui.screens.auth.LoginScreen
import com.pminteractive.pmtours.ui.screens.driverGuia.CalendarioScreen
import com.pminteractive.pmtours.ui.screens.operador.PerfilScreen
import com.pminteractive.pmtours.ui.screens.operador.ServiciosScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Guarda en memoria al usuario que inició sesión
    var usuarioSesion by remember { mutableStateOf<Usuario?>(null) }

    // Extrae solo el primer nombre (ej: "Carlos Morales" -> "Carlos")
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
                    // Guardamos la instancia del usuario autenticado
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
                nombreUsuario = primerNombre, // <-- Pasa el nombre dinámico real
                onServicioClick = { /* Próximo paso: Detalle */ },
                onCambiarTab = cambiarTab
            )
        }

        // 3. Tab Calendario
        composable(Screen.Calendario.route) {
            CalendarioScreen(
                onCambiarTab = cambiarTab
            )
        }

        // 4. Tab Perfil
        composable(Screen.Perfil.route) {
            PerfilScreen(
                onCambiarTab = cambiarTab,
                onCerrarSesion = {
                    usuarioSesion = null // Limpia la sesión activa
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Servicios.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
package com.pminteractive.pmtours.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // <-- IMPORT AÑADIDO
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.LinceBackground
import com.pminteractive.pmtours.ui.theme.LinceCyan
import com.pminteractive.pmtours.ui.theme.LinceTextSecondary

@Composable
fun AdminBottomBar(
    rutaActual: String,
    onNavegar: (String) -> Unit
) {
    val txtUsuarios = stringResource(id = R.string.nav_admin_usuarios)
    val txtServicios = stringResource(id = R.string.nav_admin_servicios)
    val txtCrear = stringResource(id = R.string.nav_admin_crear)
    val txtPerfil = stringResource(id = R.string.nav_admin_perfil)

    NavigationBar(
        containerColor = LinceBackground,
        tonalElevation = 8.dp
    ) {
        // 1. Pestaña Usuarios
        NavigationBarItem(
            selected = rutaActual == Screen.PanelAdministrador.route,
            onClick = {
                if (rutaActual != Screen.PanelAdministrador.route) onNavegar(Screen.PanelAdministrador.route)
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = txtUsuarios,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(txtUsuarios, fontSize = 11.sp) },
            colors = coloresLinceNav()
        )

        // 2. Pestaña Servicios (Independiente)
        NavigationBarItem(
            selected = rutaActual == Screen.ServiciosAdministrador.route,
            onClick = {
                if (rutaActual != Screen.ServiciosAdministrador.route) onNavegar(Screen.ServiciosAdministrador.route)
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_map),
                    contentDescription = txtServicios,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(txtServicios, fontSize = 11.sp) },
            colors = coloresLinceNav()
        )

        // 3. Botón Central [+] (Lleva directo a la pantalla dual de creación)
        NavigationBarItem(
            selected = rutaActual == Screen.CrearAdministrador.route,
            onClick = {
                if (rutaActual != Screen.CrearAdministrador.route) onNavegar(Screen.CrearAdministrador.route)
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = txtCrear,
                    modifier = Modifier.size(28.dp),
                    tint = LinceCyan
                )
            },
            label = { Text(txtCrear, fontSize = 11.sp, color = LinceCyan) },
            colors = coloresLinceNav()
        )

        // 4. Pestaña Perfil
        NavigationBarItem(
            selected = rutaActual == Screen.PerfilAdministrador.route,
            onClick = {
                if (rutaActual != Screen.PerfilAdministrador.route) onNavegar(Screen.PerfilAdministrador.route)
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info), // O ic_settings
                    contentDescription = txtPerfil,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(txtPerfil, fontSize = 11.sp) },
            colors = coloresLinceNav()
        )
    }
}

@Composable
private fun coloresLinceNav() = NavigationBarItemDefaults.colors(
    selectedIconColor = LinceCyan,
    selectedTextColor = LinceCyan,
    unselectedIconColor = LinceTextSecondary,
    unselectedTextColor = LinceTextSecondary,
    indicatorColor = Color.Transparent
)
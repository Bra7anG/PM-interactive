package com.pminteractive.pmtours.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.LinceBackground
import com.pminteractive.pmtours.ui.theme.LinceCyan
import com.pminteractive.pmtours.ui.theme.LinceTextSecondary

@Composable
fun CoordinadorBottomBar(
    rutaActual: String,
    onNavegar: (String) -> Unit
) {
    NavigationBar(
        containerColor = LinceBackground,
        tonalElevation = 8.dp
    ) {
        // Tab 1: Inicio (Monitor de servicios, transportistas y guías)
        NavigationBarItem(
            selected = rutaActual == Screen.PanelCoordinador.route,
            modifier = Modifier.testTag("tab_coord_inicio"),
            onClick = {
                if (rutaActual != Screen.PanelCoordinador.route) {
                    onNavegar(Screen.PanelCoordinador.route)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Inicio",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Inicio", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LinceCyan,
                selectedTextColor = LinceCyan,
                unselectedIconColor = LinceTextSecondary,
                unselectedTextColor = LinceTextSecondary,
                indicatorColor = Color.Transparent
            )
        )

        // Tab 2: Crear Servicio
        NavigationBarItem(
            selected = rutaActual == Screen.CrearServicioCoordinador.route,
            modifier = Modifier.testTag("tab_coord_crear"),
            onClick = {
                if (rutaActual != Screen.CrearServicioCoordinador.route) {
                    onNavegar(Screen.CrearServicioCoordinador.route)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Crear servicio",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Crear", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LinceCyan,
                selectedTextColor = LinceCyan,
                unselectedIconColor = LinceTextSecondary,
                unselectedTextColor = LinceTextSecondary,
                indicatorColor = Color.Transparent
            )
        )

        // Tab 3: Perfil
        NavigationBarItem(
            selected = rutaActual == Screen.PerfilCoordinador.route,
            modifier = Modifier.testTag("tab_coord_perfil"),
            onClick = {
                if (rutaActual != Screen.PerfilCoordinador.route) {
                    onNavegar(Screen.PerfilCoordinador.route)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "Perfil",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Perfil", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LinceCyan,
                selectedTextColor = LinceCyan,
                unselectedIconColor = LinceTextSecondary,
                unselectedTextColor = LinceTextSecondary,
                indicatorColor = Color.Transparent
            )
        )
    }
}
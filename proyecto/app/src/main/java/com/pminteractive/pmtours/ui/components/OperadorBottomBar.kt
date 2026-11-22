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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.LinceBackground
import com.pminteractive.pmtours.ui.theme.LinceCyan
import com.pminteractive.pmtours.ui.theme.LinceTextSecondary

@Composable
fun OperadorBottomBar(
    rutaActual: String,
    onNavegar: (String) -> Unit
) {
    val textoServicios = stringResource(id = R.string.nav_operador_servicios)
    val textoCalendario = stringResource(id = R.string.nav_operador_calendario)
    val textoPerfil = stringResource(id = R.string.nav_operador_perfil)

    NavigationBar(
        containerColor = LinceBackground,
        tonalElevation = 8.dp
    ) {
        // Tab 1: Servicios (ic_home)
        NavigationBarItem(
            selected = rutaActual == Screen.Servicios.route,
            modifier = Modifier.testTag("tab_servicios"),
            onClick = {
                if (rutaActual != Screen.Servicios.route) {
                    onNavegar(Screen.Servicios.route)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = textoServicios,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(textoServicios, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LinceCyan,
                selectedTextColor = LinceCyan,
                unselectedIconColor = LinceTextSecondary,
                unselectedTextColor = LinceTextSecondary,
                indicatorColor = Color.Transparent
            )
        )

        // Tab 2: Calendario (ic_calendario)
        NavigationBarItem(
            selected = rutaActual == Screen.Calendario.route,
            modifier = Modifier.testTag("tab_calendario"),
            onClick = {
                if (rutaActual != Screen.Calendario.route) {
                    onNavegar(Screen.Calendario.route)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendario),
                    contentDescription = textoCalendario,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(textoCalendario, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LinceCyan,
                selectedTextColor = LinceCyan,
                unselectedIconColor = LinceTextSecondary,
                unselectedTextColor = LinceTextSecondary,
                indicatorColor = Color.Transparent
            )
        )

        // Tab 3: Perfil (ic_person)
        NavigationBarItem(
            selected = rutaActual == Screen.Perfil.route,
            modifier = Modifier.testTag("tab_perfil"),
            onClick = {
                if (rutaActual != Screen.Perfil.route) {
                    onNavegar(Screen.Perfil.route)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = textoPerfil,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(textoPerfil, fontSize = 11.sp) },
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
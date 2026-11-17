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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.ui.theme.LinceBackground
import com.pminteractive.pmtours.ui.theme.LinceCyan
import com.pminteractive.pmtours.ui.theme.LinceTextSecondary

@Composable
fun PanelBottomBar(
    rutaActual: String,
    rutaPrincipal: String,
    etiquetaPrincipal: String,
    rutaPerfil: String,
    onNavegar: (String) -> Unit,
    iconoPrincipal: Int = R.drawable.ic_home
) {
    NavigationBar(
        containerColor = LinceBackground,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = rutaActual == rutaPrincipal,
            onClick = {
                if (rutaActual != rutaPrincipal) {
                    onNavegar(rutaPrincipal)
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = iconoPrincipal),
                    contentDescription = etiquetaPrincipal,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(etiquetaPrincipal, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LinceCyan,
                selectedTextColor = LinceCyan,
                unselectedIconColor = LinceTextSecondary,
                unselectedTextColor = LinceTextSecondary,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = rutaActual == rutaPerfil,
            onClick = {
                if (rutaActual != rutaPerfil) {
                    onNavegar(rutaPerfil)
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

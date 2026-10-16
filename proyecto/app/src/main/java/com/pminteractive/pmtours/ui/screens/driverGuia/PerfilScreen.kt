package com.pminteractive.pmtours.ui.screens.operador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pminteractive.pmtours.ui.components.OperadorBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*

@Composable
fun PerfilScreen(
    onCerrarSesion: () -> Unit = {},
    onCambiarTab: (String) -> Unit = {}
) {
    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            OperadorBottomBar(
                rutaActual = Screen.Perfil.route,
                onNavegar = onCambiarTab
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Cabecera de Usuario
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LinceSurface)
                        .border(2.dp, LinceCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CM", color = LinceCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("Carlos Morales", color = LinceTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Driver · Sede Santiago", color = LinceTextSecondary, fontSize = 13.sp)
                    Text("18.452.123-K", color = LinceTextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ficha de Vehículo
            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🚐 Flota Asignada", color = LinceCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Mercedes-Benz Sprinter 2022", color = LinceTextPrimary, fontWeight = FontWeight.Medium)
                    Text("Patente: KLSX-45 · Capacidad: 12 pasajeros", color = LinceTextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ficha de Datos Bancarios
            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💳 Datos de Transferencia", color = LinceCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("BancoEstado · Cuenta RUT", color = LinceTextPrimary, fontWeight = FontWeight.Medium)
                    Text("N°: 18452123 · Titular: Carlos Morales", color = LinceTextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Cerrar Sesión
            OutlinedButton(
                onClick = onCerrarSesion,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceError),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceError)
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}
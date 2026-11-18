package com.pminteractive.pmtours.ui.screens.coordinador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.ui.components.PanelBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.CoordinadorViewModel

@Composable
fun PerfilCoordinadorScreen(
    usuario: Usuario?,
    onCerrarSesion: () -> Unit = {},
    onCambiarTab: (String) -> Unit = {},
    viewModel: CoordinadorViewModel = viewModel()
) {
    LaunchedEffect(usuario?.sede) {
        usuario?.sede?.let { viewModel.cargarServiciosDeSede(it) }
    }

    val resumen by viewModel.resumen.collectAsState()

    val iniciales = usuario?.nombreCompleto?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString() }
        ?.take(2)
        ?.joinToString("") ?: "CO"

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            PanelBottomBar(
                rutaActual = Screen.PerfilCoordinador.route,
                rutaPrincipal = Screen.PanelCoordinador.route,
                etiquetaPrincipal = "Panel",
                rutaPerfil = Screen.PerfilCoordinador.route,
                onNavegar = onCambiarTab
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(LinceSurface)
                        .border(2.dp, LinceCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iniciales,
                        color = LinceCyan,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = usuario?.nombreCompleto ?: "Coordinador",
                        color = LinceTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${usuario?.rol?.name ?: "COORDINADOR"} · Base ${usuario?.sede?.nombreCiudad ?: "Santiago"}",
                        color = LinceTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "RUT: ${usuario?.rut ?: "15.321.654-8"}",
                        color = LinceTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(LinceCyan.copy(alpha = 0.5f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Resumen de supervisión",
                        color = LinceCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricaSupervision(
                            valor = resumen.pendientes,
                            etiqueta = "Pendientes",
                            color = LincePending,
                            modifier = Modifier.weight(1f)
                        )
                        MetricaSupervision(
                            valor = resumen.confirmados,
                            etiqueta = "Confirmados",
                            color = LinceSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        MetricaSupervision(
                            valor = resumen.rechazados,
                            etiqueta = "Rechazados",
                            color = LinceDanger,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Datos de contacto",
                        color = LinceCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Correo: ${usuario?.correo ?: "luis.figueroa@pmtours.cl"}",
                        color = LinceTextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Teléfono: ${usuario?.telefono ?: "+56999881122"}",
                        color = LinceTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Dirección: ${usuario?.direccion ?: "Moneda 920, Santiago Centro"}",
                        color = LinceTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick = onCerrarSesion,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceError),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(LinceError)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Cerrar Sesión Institucional",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricaSupervision(
    valor: Int,
    etiqueta: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LinceBackground,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = valor.toString(),
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = etiqueta,
                color = LinceTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

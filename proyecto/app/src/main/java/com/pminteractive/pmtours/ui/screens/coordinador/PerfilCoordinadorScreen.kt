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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.ui.components.CoordinadorBottomBar
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
    val iniciales = usuario?.nombreCompleto?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString() }
        ?.take(2)
        ?.joinToString("") ?: "CO"

    val sedeBase = usuario?.sede?.nombreCiudad ?: "Santiago"
    val codigoSede = usuario?.sede?.codigo ?: "SCL"

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            CoordinadorBottomBar(
                rutaActual = Screen.PerfilCoordinador.route,
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

            // Cabecera de identidad
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
                        text = "Coordinador de Operaciones · Base $sedeBase",
                        color = LinceTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "ID: ${usuario?.id ?: "CRD-201"}",
                        color = LinceCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Tarjeta de Alcance Operativo
            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(LinceCyan.copy(alpha = 0.5f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Alcance operativo",
                        color = LinceCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    FilaPerfilInfo(
                        etiqueta = "Sede principal",
                        valor = "$sedeBase ($codigoSede)"
                    )
                    FilaPerfilInfo(
                        etiqueta = "Cobertura de red",
                        valor = "Santiago, Puerto Varas y Punta Arenas"
                    )
                    FilaPerfilInfo(
                        etiqueta = "Atribuciones del rol",
                        valor = "Creación y asignación de servicios, supervisión de flota/guías y cancelación operativa"
                    )
                    FilaPerfilInfo(
                        etiqueta = "Sincronización",
                        valor = "Conectado a Lince API · Southbound"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Datos institucionales
            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Contacto institucional",
                        color = LinceCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    FilaPerfilInfo(
                        etiqueta = "Correo",
                        valor = usuario?.correo ?: "luis.figueroa@pmtours.cl"
                    )
                    FilaPerfilInfo(
                        etiqueta = "Teléfono",
                        valor = usuario?.telefono ?: "+56999881122"
                    )
                    FilaPerfilInfo(
                        etiqueta = "Oficina",
                        valor = usuario?.direccion ?: "Moneda 920, Santiago Centro"
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
private fun FilaPerfilInfo(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta,
            color = LinceTextSecondary,
            fontSize = 11.sp
        )
        Text(
            text = valor,
            color = LinceTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
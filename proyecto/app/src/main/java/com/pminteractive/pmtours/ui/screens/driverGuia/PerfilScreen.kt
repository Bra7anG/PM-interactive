/**
 * OBJETIVO DEL SCRIPT:
 * Pantalla de perfil de usuario, resumen de liquidación mensual y ficha técnica de operador.
 *
 * RESUMEN:
 * Despliega los datos personales del trabajador, la información de transferencia bancaria,
 * los activos asignados (vehículos o competencias de guía) y un dashboard visual de liquidación
 * con métricas modulares de servicios completados y tarifas resguardadas.
 *
 * DETALLE:
 * - Estructura los ingresos del mes mediante tarjetas de métricas estadísticas (KPIs).
 * - Distingue con claridad gráfica los servicios completados vs las cancelaciones con pago protegido.
 * - Utiliza el vector nativo 'ic_info' para notas aclaratorias sin depender de emojis.
 * - Muestra la flota habilitada si el rol es Driver o especialidades/idiomas si es Guía.
 */
package com.pminteractive.pmtours.ui.screens.driverGuia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.operadores.Driver
import com.pminteractive.pmtours.model.operadores.Guia
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.ui.components.OperadorBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.ServicioViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PerfilScreen(
    usuario: Usuario?,
    onCerrarSesion: () -> Unit = {},
    onCambiarTab: (String) -> Unit = {},
    servicioViewModel: ServicioViewModel = viewModel()
) {
    val resumenLiquidacion by servicioViewModel.resumenMesActual.collectAsState()

    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
    }
    val montoFormateado = formatoMoneda.format(resumenLiquidacion.totalAcumulado)

    val iniciales = usuario?.nombreCompleto?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString() }
        ?.take(2)?.joinToString("") ?: "OP"

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Cabecera con Avatar e Identidad del Trabajador
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
                        text = usuario?.nombreCompleto ?: "Carlos Morales",
                        color = LinceTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${usuario?.rol?.name ?: "DRIVER"} · Base ${usuario?.sede?.nombreCiudad ?: "Santiago"}",
                        color = LinceTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "RUT: ${usuario?.rut ?: "18.452.123-K"}",
                        color = LinceTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 2. Tarjeta de Liquidación Rediseñada (Estilo Dashboard)
            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceCyan.copy(alpha = 0.5f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    // Encabezado del balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Liquidación Acumulada",
                            color = LinceCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = LinceBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                            )
                        ) {
                            Text(
                                text = resumenLiquidacion.nombreMes.ifEmpty { "Octubre 2026" },
                                color = LinceTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = montoFormateado,
                        color = LinceTextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tarjetas de Métricas Estadísticas (KPIs)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tarjeta 1: Servicios Finalizados
                        Surface(
                            color = LinceBackground,
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "${resumenLiquidacion.serviciosCompletados}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981) // Verde éxito
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Finalizados",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LinceTextPrimary
                                )
                                Text(
                                    text = "Completados con éxito",
                                    fontSize = 10.sp,
                                    color = LinceTextSecondary
                                )
                            }
                        }

                        // Tarjeta 2: Cancelaciones Protegidas
                        Surface(
                            color = LinceBackground,
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "${resumenLiquidacion.serviciosCanceladosConPago}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B) // Ámbar informativo
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tarifa protegida",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LinceTextPrimary
                                )
                                Text(
                                    text = "No-show / Cliente",
                                    fontSize = 10.sp,
                                    color = LinceTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nota al pie con ícono vectorial 'ic_info'
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = null,
                            tint = LinceTextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Montos acumulados con Check-Out completado y servicios con tarifa resguardada por la agencia.",
                            color = LinceTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Ficha de Datos de Transferencia Bancaria
            val datosBancarios = usuario?.datosBancarios
            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Datos de Transferencia Bancaria",
                        color = LinceCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${datosBancarios?.banco ?: "BancoEstado"} · ${datosBancarios?.tipoCuenta ?: "Cuenta RUT"}",
                        color = LinceTextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "N°: ${datosBancarios?.numeroCuenta ?: "18452123"}",
                        color = LinceTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Titular: ${datosBancarios?.titular ?: "Carlos Morales"}",
                        color = LinceTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Sección según rol (Vehículos para Driver, Idiomas para Guía)
            when (usuario) {
                is Driver -> {
                    Surface(
                        color = LinceSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Flota de Vehículos Habilitada",
                                color = LinceCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            usuario.vehiculos.forEachIndexed { index, vehiculo ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        color = LinceSurfaceBorder,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                Text(
                                    text = "${vehiculo.modelo} (${vehiculo.tipo})",
                                    color = LinceTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Patente: ${vehiculo.patente} · Capacidad: ${vehiculo.capacidadPasajeros} pasajeros",
                                    color = LinceTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                is Guia -> {
                    Surface(
                        color = LinceSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Competencias e Idiomas",
                                color = LinceCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Idiomas: ${usuario.idiomas.joinToString(", ")}",
                                color = LinceTextPrimary,
                                fontSize = 14.sp
                            )
                            if (usuario.especialidades.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Especialidades: ${usuario.especialidades.joinToString(" · ")}",
                                    color = LinceTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 5. Botón de Cerrar Sesión
            OutlinedButton(
                onClick = onCerrarSesion,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceError),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceError)
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
/**
 * OBJETIVO DEL SCRIPT:
 * Pantalla de visualización detallada y gestión operativa de un servicio turístico para Driver o Guía.
 *
 * RESUMEN:
 * Renderiza la ficha técnica completa del servicio con lenguaje accesible,
 * ilustración gráfica de la ruta en Canvas, apertura de navegación externa con botones
 * anchos e íconos vectoriales, y validación estricta de Check-in para el mismo día del servicio
 * incorporando indicador visual de bloqueo mediante vector nativo.
 *
 * DETALLE:
 * - Refleja la transición de asignación (statusPL "RQ" a "CF") mediante el botón de confirmación de recepción.
 * - Traduce la nomenclatura técnica de pasajeros ('paxTexto') a español legible (adultos y niños).
 * - Utiliza Vector Drawables nativos ('ic_location_pin', 'ic_map' e 'ic_lock') para la iconografía.
 * - Restringe la activación del Check-in únicamente a la fecha en curso, mostrando tarjeta informativa con candado si es a futuro.
 */
package com.pminteractive.pmtours.ui.screens.driverGuia

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.ServicioViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DetalleServicioScreen(
    servicioId: String,
    onVolver: () -> Unit,
    viewModel: ServicioViewModel = viewModel()
) {
    val context = LocalContext.current
    val servicios by viewModel.servicios.collectAsState()
    val servicio = servicios.find { it.id == servicioId }

    Scaffold(
        containerColor = LinceBackground
    ) { innerPadding ->
        if (servicio == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Servicio no encontrado", color = LinceTextSecondary)
            }
            return@Scaffold
        }

        // Formato chileno de fecha: DD-MM-YYYY
        val fechaChilena = remember(servicio.fecha) {
            try {
                val fechaParsed = LocalDate.parse(servicio.fecha)
                fechaParsed.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            } catch (e: Exception) {
                servicio.fecha
            }
        }

        // Validación: Solo habilitar Check-in si el servicio corresponde al día de hoy
        val esHoy = remember(servicio.fecha) {
            try {
                LocalDate.now() == LocalDate.parse(servicio.fecha)
            } catch (e: Exception) {
                true
            }
        }

        // Traducción de pasajeros a texto claro
        val textoPasajeros = remember(servicio.cantidadPasajeros, servicio.paxTexto) {
            val partes = servicio.paxTexto.split("+")
            if (partes.size == 2) {
                val adultos = partes[0].toIntOrNull() ?: servicio.cantidadPasajeros
                val ninos = partes[1].toIntOrNull() ?: 0
                if (ninos > 0) {
                    "${servicio.cantidadPasajeros} personas ($adultos adultos, $ninos niños)"
                } else {
                    "${servicio.cantidadPasajeros} adultos"
                }
            } else {
                "${servicio.cantidadPasajeros} pasajeros"
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Barra superior
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVolver() }
            ) {
                Text(
                    text = "‹",
                    fontSize = 32.sp,
                    color = LinceTextPrimary,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Detalle del servicio",
                    color = LinceTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Curva gráfica de ruta (Canvas nativo)
            Surface(
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val startPoint = Offset(16f, h * 0.75f)
                    val endPoint = Offset(w - 16f, h * 0.25f)
                    val control1 = Offset(w * 0.35f, 0f)
                    val control2 = Offset(w * 0.65f, h)

                    val path = Path().apply {
                        moveTo(startPoint.x, startPoint.y)
                        cubicTo(control1.x, control1.y, control2.x, control2.y, endPoint.x, endPoint.y)
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF1E3A8A),
                        style = Stroke(
                            width = 6f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                    )

                    drawCircle(color = Color(0xFF1E3A8A), radius = 10f, center = startPoint)
                    drawCircle(color = Color(0xFFEA580C), radius = 10f, center = endPoint)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Ficha con datos operativos generales
            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FilaDetalle(etiqueta = "N° Reserva", valor = servicio.numeroReserva)
                    FilaDetalle(etiqueta = "Fecha", valor = fechaChilena, destacado = true)
                    FilaDetalle(etiqueta = "Hora de salida", valor = "${servicio.horaInicio} - ${servicio.horaFinEstimada} hrs")
                    FilaDetalle(etiqueta = "Tipo de servicio", valor = servicio.tipo.descripcion)
                    FilaDetalle(etiqueta = "Pasajeros", valor = textoPasajeros, destacado = true)
                    FilaDetalle(etiqueta = "Tarifa acordada", valor = "$${servicio.tarifaAcordada.toInt()}")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Bloques de Ubicación con botones amplios e íconos vectoriales
            Text(
                text = "Puntos de Ruta",
                color = LinceTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Tarjeta de Pickup
            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location_pin),
                            contentDescription = null,
                            tint = LinceCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lugar de Recogida (Pickup)",
                            color = LinceCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = servicio.lugarPickup,
                        color = LinceTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { abrirMapaExterno(servicio.lugarPickup, context) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_map),
                            contentDescription = null,
                            tint = LinceCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ver en el mapa",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LinceTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tarjeta de Destino
            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location_pin),
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lugar de Destino",
                            color = Color(0xFFEA580C),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = servicio.lugarDropOff,
                        color = LinceTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { abrirMapaExterno(servicio.lugarDropOff, context) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_map),
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ver en el mapa",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LinceTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Observaciones
            Text(
                text = "Observaciones de Agencia",
                color = LinceTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (servicio.notasAgencia.isNotBlank()) servicio.notasAgencia else "Sin notas adicionales.",
                    color = LinceTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Indicador de Check-in registrado
            if (servicio.checkIn != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = if (servicio.checkIn.aTiempo) Color(0x2610B981) else LinceErrorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (servicio.checkIn.aTiempo) "✓" else "!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (servicio.checkIn.aTiempo) Color(0xFF10B981) else LinceError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (servicio.checkIn.aTiempo) {
                                "Check-in registrado a tiempo (+15 min de anticipación)"
                            } else {
                                "Check-in registrado fuera del plazo de anticipación"
                            },
                            color = if (servicio.checkIn.aTiempo) Color(0xFF10B981) else LinceError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Botones operativos dinámicos
            when (servicio.estado) {
                EstadoServicio.ASIGNADO -> {
                    Button(
                        onClick = { viewModel.responderAsignacion(servicio.id, aceptar = true) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LinceCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Confirmar Recepción del Servicio", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = { /* Contacto con coordinación */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "¿Tienes un inconveniente? Contactar a Coordinación",
                            color = LinceTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                EstadoServicio.CONFIRMADO -> {
                    if (esHoy) {
                        Button(
                            onClick = { viewModel.realizarCheckIn(servicio.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LinceCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Iniciar Servicio (Check-In GPS)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else {
                        // Tarjeta con vector de candado para fechas futuras
                        Surface(
                            color = LinceSurfaceBorder.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lock),
                                    contentDescription = null,
                                    tint = LinceTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Check-in programado",
                                        color = LinceTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Se habilitará el día del servicio ($fechaChilena).",
                                        color = LinceTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                EstadoServicio.EN_RUTA -> {
                    Button(
                        onClick = { viewModel.realizarCheckOut(servicio.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Finalizar Servicio (Check-Out)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                EstadoServicio.FINALIZADO -> {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Servicio completado exitosamente",
                            color = LinceTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(14.dp),
                            fontSize = 14.sp
                        )
                    }
                }

                EstadoServicio.CANCELADO -> {
                    Surface(
                        color = LinceErrorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Servicio cancelado por coordinación o cliente (Tarifa resguardada)",
                            color = LinceError,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(14.dp),
                            fontSize = 13.sp
                        )
                    }
                }

                else -> Unit
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun FilaDetalle(
    etiqueta: String,
    valor: String,
    destacado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            color = LinceTextSecondary,
            fontSize = 14.sp
        )
        Text(
            text = valor,
            color = LinceTextPrimary,
            fontSize = 14.sp,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(0.60f)
        )
    }
}

private fun abrirMapaExterno(direccion: String, context: Context) {
    if (direccion.isBlank()) return

    val direccionLimpia = direccion.substringBefore("(").trim()
    val consulta = Uri.encode("$direccionLimpia, Chile")
    val gmmIntentUri = Uri.parse("geo:0,0?q=$consulta")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(mapIntent)
    } catch (e: ActivityNotFoundException) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$consulta")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(webIntent)
    }
}
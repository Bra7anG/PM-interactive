/**
 * OBJETIVO DEL SCRIPT:
 * Pantalla principal de agenda y hoja de ruta de servicios para el operador (Driver o Guía).
 *
 * RESUMEN:
 * Renderiza la lista de asignaciones operativas con buscador de texto libre (botón X para limpiar),
 * chips de filtrado por estado y un banner informativo dinámico cuando se consultan los servicios completados.
 *
 * DETALLE:
 * - Filtro combinado en memoria: cruza texto libre (hotel, pasajero, reserva) con el estado seleccionado.
 * - Regla de visibilidad histórica: al activar el chip 'Finalizados', despliega un banner indicando
 *   el mes en curso y el total de servicios completados computados.
 * - Servicios pendientes y confirmados permanecen visibles sin restricción de fecha para evitar no-shows a fin de mes.
 */
package com.pminteractive.pmtours.ui.screens.driverGuia

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.ui.components.OperadorBottomBar
import com.pminteractive.pmtours.ui.components.ServicioCard
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.ServicioViewModel

enum class FiltroEstadoServicio(val etiqueta: String, val estado: EstadoServicio?) {
    TODOS("Todos", null),
    PENDIENTES("Pendientes", EstadoServicio.ASIGNADO),
    CONFIRMADOS("Confirmados", EstadoServicio.CONFIRMADO),
    EN_RUTA("En ruta", EstadoServicio.EN_RUTA),
    FINALIZADOS("Finalizados", EstadoServicio.FINALIZADO)
}

@Composable
fun ServiciosScreen(
    usuarioId: String = "TRA-001",
    nombreUsuario: String = "Carlos",
    onServicioClick: (Servicio) -> Unit = {},
    onCambiarTab: (String) -> Unit = {},
    viewModel: ServicioViewModel = viewModel()
) {
    // Sincroniza la consulta con el usuario autenticado
    LaunchedEffect(usuarioId) {
        viewModel.cargarServiciosDelUsuario(usuarioId)
    }

    val servicios by viewModel.servicios.collectAsState()
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf(FiltroEstadoServicio.TODOS) }

    // Fecha para el saludo principal
    val fechaCabecera = remember {
        try {
            val hoy = java.time.LocalDate.now()
            val formato = java.time.format.DateTimeFormatter.ofPattern(
                "EEEE d 'de' MMMM",
                java.util.Locale("es", "CL")
            )
            hoy.format(formato).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "Hoy"
        }
    }

    // Nombre del mes para el banner de auditoría
    val nombreMesActual = remember {
        try {
            val hoy = java.time.LocalDate.now()
            val formato = java.time.format.DateTimeFormatter.ofPattern("MMMM", java.util.Locale("es", "CL"))
            hoy.format(formato).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "este mes"
        }
    }

    // Filtrado reactivo combinado: texto + estado del chip
    val serviciosFiltrados = remember(servicios, textoBusqueda, filtroSeleccionado) {
        servicios.filter { servicio ->
            val coincideTexto = if (textoBusqueda.isBlank()) true else {
                val q = textoBusqueda.trim().lowercase()
                servicio.titulo.lowercase().contains(q) ||
                        servicio.numeroReserva.lowercase().contains(q) ||
                        servicio.lugarPickup.lowercase().contains(q) ||
                        servicio.lugarDropOff.lowercase().contains(q) ||
                        servicio.detalleItinerario.lowercase().contains(q)
            }

            val coincideEstado = if (filtroSeleccionado.estado == null) true else {
                servicio.estado == filtroSeleccionado.estado
            }

            coincideTexto && coincideEstado
        }
    }

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            OperadorBottomBar(
                rutaActual = Screen.Servicios.route,
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
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Cabecera con fecha y saludo
            Text(
                text = fechaCabecera,
                color = LinceTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Hola, $nombreUsuario",
                color = LinceTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Buscador con botón de limpieza instantánea (X)
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = {
                    Text(
                        text = "Buscar hotel, destino o reserva...",
                        color = LinceTextSecondary.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Buscar",
                        tint = LinceTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Limpiar texto",
                                tint = LinceTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LinceSurface,
                    unfocusedContainerColor = LinceSurface,
                    focusedTextColor = LinceTextPrimary,
                    unfocusedTextColor = LinceTextPrimary,
                    cursorColor = LinceCyan,
                    focusedBorderColor = LinceCyan,
                    unfocusedBorderColor = LinceSurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Fila de Chips de filtrado rápido
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroEstadoServicio.values().forEach { filtro ->
                    val seleccionado = filtro == filtroSeleccionado
                    FilterChip(
                        selected = seleccionado,
                        onClick = { filtroSeleccionado = filtro },
                        label = {
                            Text(
                                text = filtro.etiqueta,
                                fontSize = 12.sp,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = LinceSurface,
                            labelColor = LinceTextSecondary,
                            selectedContainerColor = LinceCyan,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = LinceSurfaceBorder,
                            selectedBorderColor = LinceCyan,
                            enabled = true,
                            selected = seleccionado
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Banner informativo al seleccionar "Finalizados"
            if (filtroSeleccionado == FiltroEstadoServicio.FINALIZADOS) {
                Surface(
                    color = LinceSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(LinceSurfaceBorder)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = null,
                            tint = LinceCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Mostrando servicios finalizados de $nombreMesActual (${serviciosFiltrados.size} completados)",
                            color = LinceTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 5. Lista reactiva de tarjetas
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (serviciosFiltrados.isEmpty()) {
                    item {
                        Surface(
                            color = LinceSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(LinceSurfaceBorder)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_info),
                                    contentDescription = null,
                                    tint = LinceTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "No se encontraron servicios para este filtro.",
                                    color = LinceTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(serviciosFiltrados, key = { it.id }) { servicio ->
                        ServicioCard(
                            servicio = servicio,
                            onCardClick = { onServicioClick(servicio) }
                        )
                    }
                }
            }
        }
    }
}
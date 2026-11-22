/**
- OBJETIVO DEL SCRIPT:
- Pantalla principal de agenda y hoja de ruta de servicios para el operador (Driver o Guía).
 */
package com.pminteractive.pmtours.ui.screens.driverGuia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // <-- IMPORT AÑADIDO
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.network.NetworkMonitor
import com.pminteractive.pmtours.ui.components.OperadorBottomBar
import com.pminteractive.pmtours.ui.components.ServicioCard
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.ServicioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Transformamos los Filtros a una clase con identificadores de recurso (R.string...)
enum class FiltroEstadoServicio(val stringResId: Int, val estado: EstadoServicio?) {
    TODOS(R.string.filtro_todos, null),
    PENDIENTES(R.string.filtro_pendientes, EstadoServicio.ASIGNADO),
    CONFIRMADOS(R.string.filtro_confirmados, EstadoServicio.CONFIRMADO),
    EN_RUTA(R.string.filtro_en_ruta, EstadoServicio.EN_RUTA),
    FINALIZADOS(R.string.filtro_finalizados, EstadoServicio.FINALIZADO),
    CANCELADOS(R.string.filtro_cancelados, EstadoServicio.CANCELADO)
}

@Composable
fun ServiciosScreen(
    usuarioId: String = "TRA-001",
    nombreUsuario: String = "Carlos",
    onServicioClick: (Servicio) -> Unit = {},
    onCambiarTab: (String) -> Unit = {},
    viewModel: ServicioViewModel = viewModel()
) {
    LaunchedEffect(usuarioId) {
        viewModel.cargarServiciosDelUsuario(usuarioId)
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        NetworkMonitor.inicializar(context)
    }
    val estaConectado by NetworkMonitor.estaConectado.collectAsState()
    var estabaConectado by remember { mutableStateOf(estaConectado) }

    LaunchedEffect(estaConectado) {
        if (estaConectado && !estabaConectado) {
            delay(400)
            viewModel.cargarServiciosDelUsuario(usuarioId, forzarRecarga = true)
        }
        estabaConectado = estaConectado
    }

    // Listas del ViewModel
    val serviciosDelDia by viewModel.serviciosDelDia.collectAsState()
    val todosLosServicios by viewModel.servicios.collectAsState()
    val fechaSeleccionada by viewModel.fechaSeleccionada.collectAsState()

    // Generar la lista de fechas con servicios para marcarlos en el nuevo calendario popup
    val fechasConServicio = remember(todosLosServicios) {
        todosLosServicios
            .filter {
                it.estado == EstadoServicio.ASIGNADO ||
                        it.estado == EstadoServicio.CONFIRMADO ||
                        it.estado == EstadoServicio.EN_RUTA ||
                        it.estado == EstadoServicio.FINALIZADO
            }
            .map { it.fecha }
            .toSet()
    }

    var textoBusqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf(FiltroEstadoServicio.TODOS) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    // Fechas dinámicas al idioma del dispositivo
    val fallbackMes = stringResource(id = R.string.servicios_mes_actual_fallback)
    val nombreMesActual = remember {
        try {
            val hoy = LocalDate.now()
            val formato = DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())
            hoy.format(formato).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            fallbackMes
        }
    }

    val serviciosFiltrados = remember(serviciosDelDia, textoBusqueda, filtroSeleccionado) {
        serviciosDelDia.filter { servicio ->
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

    // NUEVO: Calendario personalizado con puntitos de servicio
    if (mostrarDatePicker) {
        DialogCalendarioLince(
            fechaInicial = fechaSeleccionada,
            fechasConServicio = fechasConServicio,
            onFechaSeleccionada = { nuevaFecha ->
                viewModel.seleccionarFecha(nuevaFecha)
                mostrarDatePicker = false
            },
            onDismiss = { mostrarDatePicker = false }
        )
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

            // 1. Cabecera
            Text(
                text = stringResource(id = R.string.servicios_titulo),
                color = LinceTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(id = R.string.servicios_saludo, nombreUsuario),
                color = LinceTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            if (!estaConectado) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LincePending)
                    )
                    Text(
                        text = stringResource(id = R.string.servicios_sin_conexion),
                        color = LincePending,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Barra selectora de fecha
            BarraSelectorFechaActiva(
                fecha = fechaSeleccionada,
                onDiaAnterior = { viewModel.seleccionarFecha(fechaSeleccionada.minusDays(1)) },
                onDiaSiguiente = { viewModel.seleccionarFecha(fechaSeleccionada.plusDays(1)) },
                onAbrirCalendario = { mostrarDatePicker = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Buscador
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.servicios_busqueda_hint),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("servicios_busqueda")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Fila de Chips
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
                        modifier = Modifier.testTag("chip_${filtro.name}"),
                        label = {
                            Text(
                                text = stringResource(id = filtro.stringResId),
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

            // 5. Banners informativos
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
                            text = stringResource(id = R.string.servicios_banner_finalizados, nombreMesActual, serviciosFiltrados.size),
                            color = LinceTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (filtroSeleccionado == FiltroEstadoServicio.CANCELADOS) {
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
                            tint = LinceCancelled,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.servicios_banner_cancelados, serviciosFiltrados.size),
                            color = LinceTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 6. Lista de tarjetas
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("servicios_lista")
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
                                .testTag("servicios_vacio")
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
                                    text = stringResource(id = R.string.servicios_lista_vacia),
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

@Composable
private fun BarraSelectorFechaActiva(
    fecha: LocalDate,
    onDiaAnterior: () -> Unit,
    onDiaSiguiente: () -> Unit,
    onAbrirCalendario: () -> Unit
) {
    // Usamos el idioma del dispositivo
    val textoHoy = stringResource(id = R.string.servicios_fecha_hoy, "")
    val textoManana = stringResource(id = R.string.servicios_fecha_manana, "")

    val textoFecha = remember(fecha, textoHoy, textoManana) {
        val hoy = LocalDate.now()
        val base = fecha.format(DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.getDefault()))
            .replaceFirstChar { it.uppercase() }
        when (fecha) {
            hoy -> textoHoy.replace(" · ", " · $base") // Truco limpio para reutilizar el formato
            hoy.plusDays(1) -> textoManana.replace(" · ", " · $base")
            else -> base
        }
    }

    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDiaAnterior) {
                Text("‹", color = LinceCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAbrirCalendario() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = textoFecha,
                    color = LinceTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "▾",
                    color = LinceCyan,
                    fontSize = 12.sp
                )
            }

            TextButton(onClick = onDiaSiguiente) {
                Text("›", color = LinceCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Calendario personalizado en formato modal (Dialog) para la pantalla de Servicios
@Composable
private fun DialogCalendarioLince(
    fechaInicial: LocalDate,
    fechasConServicio: Set<String>,
    onFechaSeleccionada: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val mesActual = remember { YearMonth.from(fechaInicial) }
    val inicioRango = remember { mesActual.minusMonths(12) }
    val finRango = remember { mesActual.plusMonths(12) }
    val diasSemana = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) }

    val calendarState = rememberCalendarState(
        startMonth = inicioRango,
        endMonth = finRango,
        firstVisibleMonth = mesActual,
        firstDayOfWeek = diasSemana.first()
    )
    val scope = rememberCoroutineScope()
    val visibleMonth = calendarState.firstVisibleMonth.yearMonth

    val textoMesVisible = remember(visibleMonth) {
        visibleMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())).replaceFirstChar { it.uppercase() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LinceSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Cabecera mes y flechas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { calendarState.animateScrollToMonth(visibleMonth.minusMonths(1)) } },
                        enabled = visibleMonth > inicioRango
                    ) {
                        Text("‹", color = if (visibleMonth > inicioRango) LinceTextPrimary else LinceTextSecondary.copy(alpha = 0.3f), fontSize = 24.sp)
                    }
                    Text(textoMesVisible, color = LinceTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { scope.launch { calendarState.animateScrollToMonth(visibleMonth.plusMonths(1)) } },
                        enabled = visibleMonth < finRango
                    ) {
                        Text("›", color = if (visibleMonth < finRango) LinceTextPrimary else LinceTextSecondary.copy(alpha = 0.3f), fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Días de la semana
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    diasSemana.forEach { dia ->
                        Text(
                            text = dia.getDisplayName(TextStyle.NARROW, Locale.getDefault()).uppercase(),
                            color = LinceTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grilla del Calendario
                HorizontalCalendar(
                    state = calendarState,
                    modifier = Modifier.fillMaxWidth(),
                    dayContent = { day ->
                        val tieneServicio = fechasConServicio.contains(day.date.toString())
                        val esSeleccionado = day.date == fechaInicial

                        if (day.position == DayPosition.MonthDate) {
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (esSeleccionado) LinceCyan.copy(alpha = 0.2f) else if (tieneServicio) LinceServiceSoft else Color.Transparent)
                                    .border(if (esSeleccionado) 1.dp else 0.dp, if (esSeleccionado) LinceCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { onFechaSeleccionada(day.date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.date.dayOfMonth.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = if (tieneServicio || esSeleccionado) FontWeight.Bold else FontWeight.Medium,
                                    color = if (tieneServicio) LinceService else LinceTextPrimary
                                )
                                if (tieneServicio) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 2.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(LinceService)
                                    )
                                }
                            }
                        } else {
                            Box(modifier = Modifier.padding(2.dp).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                Text(text = day.date.dayOfMonth.toString(), fontSize = 12.sp, color = LinceTextSecondary.copy(alpha = 0.3f))
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Cancelar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.calendario_btn_cancelar), color = LinceTextSecondary)
                    }
                }
            }
        }
    }
}
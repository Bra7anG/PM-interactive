/**
 * OBJETIVO DEL SCRIPT:
 * Pantalla de gestión de agenda, turnos y bloqueo de disponibilidad para Driver o Guía.
 *
 * RESUMEN:
 * Renderiza un calendario mensual interactivo con gestos de deslizamiento, selector de día
 * con interruptor (Switch) para conmutar bloqueos y deselección automática al cambiar de mes.
 *
 * DETALLE:
 * - Detecta mediante 'LaunchedEffect' el cambio de mes visible y reinicia 'fechaSeleccionada' a null.
 * - Utiliza vectores nativos 'ic_touch' e 'ic_info' en lugar de emojis informales.
 * - Valida en tiempo real que no se bloqueen días con servicios asignados o con menos de 7 días de anticipación.
 * - Mantiene actualizada la lista inferior con los próximos días bloqueados confirmados.
 */
package com.pminteractive.pmtours.ui.screens.driverGuia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.ui.components.OperadorBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.CalendarioViewModel
import com.pminteractive.pmtours.viewmodel.ServicioViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarioScreen(
    usuario: Usuario? = null,
    onCambiarTab: (String) -> Unit = {},
    servicioViewModel: ServicioViewModel = viewModel(),
    calendarioViewModel: CalendarioViewModel = viewModel()
) {
    LaunchedEffect(usuario?.id) {
        usuario?.id?.let {
            servicioViewModel.cargarServiciosDelUsuario(it)
            calendarioViewModel.cargarDiasBloqueados(it)
        }
    }

    val serviciosAsignados by servicioViewModel.servicios.collectAsState()
    val diasBloqueados by calendarioViewModel.diasBloqueados.collectAsState()
    val fechasConServicio = remember(serviciosAsignados) {
        serviciosAsignados
            .filter {
                it.estado == EstadoServicio.ASIGNADO ||
                    it.estado == EstadoServicio.CONFIRMADO ||
                    it.estado == EstadoServicio.EN_RUTA ||
                    it.estado == EstadoServicio.FINALIZADO
            }
            .map { it.fecha }
            .toSet()
    }

    val mesActual = remember { YearMonth.now() }
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

    val hoy = remember { LocalDate.now() }
    val fechaMinimaBloqueo = remember(hoy) { hoy.plusDays(7) }

    var fechaSeleccionada by remember { mutableStateOf<LocalDate?>(null) }

    val visibleMonth = calendarState.firstVisibleMonth.yearMonth
    val textoMesVisible = remember(visibleMonth) {
        visibleMonth.format(
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "CL"))
        ).replaceFirstChar { it.uppercase() }
    }

    LaunchedEffect(visibleMonth) {
        if (fechaSeleccionada != null && YearMonth.from(fechaSeleccionada) != visibleMonth) {
            fechaSeleccionada = null
        }
    }

    val subtituloUsuario = remember(usuario) {
        val nombre = usuario?.nombreCompleto ?: "Carlos Morales"
        val rol = usuario?.rol?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Driver"
        "$nombre · $rol"
    }

    val textoFechaSeleccionada = remember(fechaSeleccionada) {
        fechaSeleccionada?.let {
            val formato = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "CL"))
            it.format(formato)
        }
    }

    val tieneServicioSeleccionado = remember(fechaSeleccionada, fechasConServicio) {
        fechaSeleccionada?.let { fecha ->
            fechasConServicio.contains(fecha.toString())
        } ?: false
    }
    val esFechaPasada = remember(fechaSeleccionada, hoy) {
        fechaSeleccionada?.isBefore(hoy) ?: false
    }
    val cumpleAnticipacion = remember(fechaSeleccionada, fechaMinimaBloqueo) {
        fechaSeleccionada?.let { !it.isBefore(fechaMinimaBloqueo) } ?: false
    }
    val puedeBloquear = fechaSeleccionada != null && !tieneServicioSeleccionado && !esFechaPasada && cumpleAnticipacion
    val estaBloqueado = fechaSeleccionada != null && diasBloqueados.contains(fechaSeleccionada)

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            OperadorBottomBar(
                rutaActual = Screen.Calendario.route,
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
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Cabecera
            Text(
                text = subtituloUsuario,
                color = LinceTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Mi disponibilidad",
                color = LinceTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Leyenda superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LeyendaEstado(color = LincePrimary, texto = "Disponible")
                LeyendaEstado(color = LinceService, texto = "Con servicio")
                LeyendaEstado(color = LinceBlockedBorder, texto = "Bloqueado", conCandado = true)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2.1 Selector del mes visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            calendarState.animateScrollToMonth(visibleMonth.minusMonths(1))
                        }
                    },
                    enabled = visibleMonth > inicioRango
                ) {
                    Text(
                        text = "‹",
                        color = if (visibleMonth > inicioRango) {
                            LinceTextPrimary
                        } else {
                            LinceTextSecondary.copy(alpha = 0.3f)
                        },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                Text(
                    text = textoMesVisible,
                    color = LinceTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        scope.launch {
                            calendarState.animateScrollToMonth(visibleMonth.plusMonths(1))
                        }
                    },
                    enabled = visibleMonth < finRango
                ) {
                    Text(
                        text = "›",
                        color = if (visibleMonth < finRango) {
                            LinceTextPrimary
                        } else {
                            LinceTextSecondary.copy(alpha = 0.3f)
                        },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Fila de Días de la Semana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                diasSemana.forEach { dia ->
                    val inicial = dia.getDisplayName(TextStyle.NARROW, Locale("es", "CL")).uppercase()
                    Text(
                        text = inicial,
                        color = LinceTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Calendario
            HorizontalCalendar(
                state = calendarState,
                modifier = Modifier.fillMaxWidth(),
                dayContent = { day ->
                    CeldaDiaCalendario(
                        day = day,
                        esSeleccionado = day.date == fechaSeleccionada,
                        estaBloqueado = diasBloqueados.contains(day.date),
                        tieneServicio = fechasConServicio.contains(day.date.toString()),
                        onClick = { fechaSeleccionada = day.date }
                    )
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Tarjeta interactiva con Switch o Estado de espera con ic_touch
            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (fechaSeleccionada != null && textoFechaSeleccionada != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = textoFechaSeleccionada,
                                color = LinceTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (estaBloqueado) "Día marcado como libre" else "Día disponible",
                                color = if (estaBloqueado) LinceBlockedBorder else LincePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = estaBloqueado,
                            enabled = puedeBloquear || estaBloqueado,
                            onCheckedChange = { activar ->
                                fechaSeleccionada?.let { fecha ->
                                    calendarioViewModel.alternarBloqueo(fecha, activar)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = LinceBlocked,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = LinceSurfaceBorder
                            )
                        )
                    }
                } else {
                    // Estado vacío con ícono vectorial 'ic_touch'
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_touch),
                            contentDescription = null,
                            tint = LinceCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Toca un día del calendario para gestionar tu turno",
                            color = LinceTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Advertencia contextual con 'ic_info'
            if (fechaSeleccionada != null && !puedeBloquear && !estaBloqueado) {
                Spacer(modifier = Modifier.height(8.dp))
                val motivoError = when {
                    tieneServicioSeleccionado -> "No puedes bloquear este día: ya tienes un servicio asignado."
                    esFechaPasada -> "No puedes bloquear una fecha que ya pasó."
                    else -> "Los días libres deben solicitarse con al menos 1 semana de anticipación."
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = null,
                        tint = LinceWarning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = motivoError,
                        color = LinceWarning,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 6. Sección: Próximos días bloqueados
            Text(
                text = "Próximos días bloqueados",
                color = LinceTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val proximosBloqueos = remember(diasBloqueados) {
                diasBloqueados.filter { !it.isBefore(hoy) }.sorted()
            }

            if (proximosBloqueos.isEmpty()) {
                Surface(
                    color = LinceSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No tienes días libres bloqueados próximamente.",
                        color = LinceTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    proximosBloqueos.forEach { fecha ->
                        val formatoItem = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "CL"))
                        Surface(
                            color = LinceSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = fecha.format(formatoItem),
                                    color = LinceTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lock),
                                        contentDescription = null,
                                        tint = LinceBlockedBorder,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Día libre",
                                        color = LinceBlockedBorder,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CeldaDiaCalendario(
    day: CalendarDay,
    esSeleccionado: Boolean,
    estaBloqueado: Boolean,
    tieneServicio: Boolean,
    onClick: () -> Unit
) {
    if (day.position == DayPosition.MonthDate) {
        val fondoCelda = when {
            tieneServicio -> LinceServiceSoft
            estaBloqueado -> LinceBlockedStrong
            else -> LinceSurface
        }

        val colorTexto = when {
            tieneServicio -> LinceService
            estaBloqueado -> LinceBlockedSoft
            else -> LinceTextPrimary
        }

        val bordeModifier = when {
            esSeleccionado -> Modifier.border(2.dp, LinceSelectedBorder, RoundedCornerShape(10.dp))
            estaBloqueado && !tieneServicio -> Modifier.border(1.dp, LinceBlockedBorder, RoundedCornerShape(10.dp))
            else -> Modifier
        }

        Box(
            modifier = Modifier
                .padding(3.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(fondoCelda)
                .then(bordeModifier)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (tieneServicio || estaBloqueado || esSeleccionado) FontWeight.Bold else FontWeight.Medium,
                color = colorTexto
            )

            if (tieneServicio) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 3.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(LinceService)
                )
            }

            if (estaBloqueado && !tieneServicio) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = null,
                    tint = LinceBlockedSoft,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(9.dp)
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 13.sp,
                color = LinceTextSecondary.copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
private fun LeyendaEstado(
    color: Color,
    texto: String,
    conCandado: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (conCandado) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            color = LinceTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
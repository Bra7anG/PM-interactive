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
import com.pminteractive.pmtours.ui.components.OperadorBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.ServicioViewModel
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
    servicioViewModel: ServicioViewModel = viewModel()
) {
    val serviciosAsignados by servicioViewModel.servicios.collectAsState()

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

    val hoy = remember { LocalDate.now() }
    val fechaMinimaBloqueo = remember(hoy) { hoy.plusDays(7) }

    var diasBloqueados by remember {
        mutableStateOf(
            setOf(
                hoy.plusDays(9),
                hoy.plusDays(10)
            )
        )
    }

    var fechaSeleccionada by remember { mutableStateOf<LocalDate?>(null) }

    val visibleMonth = calendarState.firstVisibleMonth.yearMonth

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

    val tieneServicioSeleccionado = remember(fechaSeleccionada, serviciosAsignados) {
        fechaSeleccionada?.let { fecha ->
            serviciosAsignados.any { it.fecha == fecha.toString() }
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
                horizontalArrangement = Arrangement.Start
            ) {
                LeyendaPunto(color = Color(0xFFEF4444), texto = "Bloqueado")
                Spacer(modifier = Modifier.width(20.dp))
                LeyendaPunto(color = Color(0xFFF59E0B), texto = "Con servicio")
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                        tieneServicio = serviciosAsignados.any { it.fecha == day.date.toString() },
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
                                color = if (estaBloqueado) Color(0xFFEF4444) else LinceCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = estaBloqueado,
                            enabled = puedeBloquear || estaBloqueado,
                            onCheckedChange = { activar ->
                                diasBloqueados = if (activar) {
                                    diasBloqueados + fechaSeleccionada!!
                                } else {
                                    diasBloqueados - fechaSeleccionada!!
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEF4444),
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
                        tint = LinceTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = motivoError,
                        color = LinceTextSecondary,
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
                                Text(
                                    text = "Día libre",
                                    color = Color(0xFFEF4444),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
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
            tieneServicio -> Color(0xFFFEF3C7)
            estaBloqueado -> Color(0xFFFEE2E2)
            else -> LinceSurface
        }

        val colorTexto = when {
            tieneServicio -> Color(0xFFB45309)
            estaBloqueado -> Color(0xFFDC2626)
            else -> LinceTextPrimary
        }

        val bordeModifier = if (esSeleccionado) {
            Modifier.border(2.dp, Color(0xFFF97316), RoundedCornerShape(10.dp))
        } else {
            Modifier
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
private fun LeyendaPunto(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = texto,
            color = LinceTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
package com.pminteractive.pmtours.ui.screens.driverGuia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.pminteractive.pmtours.ui.components.OperadorBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
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
    onCambiarTab: (String) -> Unit = {},
    servicioViewModel: ServicioViewModel = viewModel()
) {
    val serviciosAsignados by servicioViewModel.servicios.collectAsState()
    val coroutineScope = rememberCoroutineScope()

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

    var fechaSeleccionada by remember { mutableStateOf<LocalDate?>(null) }
    var diasBloqueados by remember { mutableStateOf(setOf(LocalDate.now().plusDays(10))) }
    var mostrarDialogoBloqueo by remember { mutableStateOf(false) }
    var mensajeAlerta by remember { mutableStateOf<String?>(null) }

    val hoy = remember { LocalDate.now() }
    val fechaMinimaBloqueo = remember(hoy) { hoy.plusDays(7) }

    val visibleMonth = calendarState.firstVisibleMonth.yearMonth
    val textoMesVisible = remember(visibleMonth) {
        val formato = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "CL"))
        visibleMonth.format(formato).replaceFirstChar { it.uppercase() }
    }

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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Calendario",
                color = LinceTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Turnos, viajes y bloqueo de días libres",
                color = LinceTextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            val anterior = calendarState.firstVisibleMonth.yearMonth.minusMonths(1)
                            calendarState.animateScrollToMonth(anterior)
                        }
                    }) {
                        Text("‹", fontSize = 24.sp, color = LinceCyan, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = textoMesVisible,
                        color = LinceTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = {
                        coroutineScope.launch {
                            val siguiente = calendarState.firstVisibleMonth.yearMonth.plusMonths(1)
                            calendarState.animateScrollToMonth(siguiente)
                        }
                    }) {
                        Text("›", fontSize = 24.sp, color = LinceCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalCalendar(
                    state = calendarState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    dayContent = { day ->
                        DiaCalendarioItem(
                            day = day,
                            hoy = hoy,
                            esSeleccionado = day.date == fechaSeleccionada,
                            estaBloqueado = diasBloqueados.contains(day.date),
                            tieneServicio = serviciosAsignados.any { it.fecha == day.date.toString() },
                            onClick = { fecha: LocalDate ->
                                fechaSeleccionada = fecha
                                mensajeAlerta = null

                                val servicioAsignado = serviciosAsignados.any { it.fecha == fecha.toString() }

                                when {
                                    servicioAsignado -> {
                                        mensajeAlerta = "No puedes bloquear este día: ya tienes un servicio asignado."
                                    }
                                    fecha.isBefore(fechaMinimaBloqueo) -> {
                                        mensajeAlerta = "Solo puedes bloquear días con al menos 1 semana de anticipación."
                                    }
                                    else -> {
                                        mostrarDialogoBloqueo = true
                                    }
                                }
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LeyendaIndicador(color = LinceCyan, texto = "Con Servicio")
                LeyendaIndicador(color = Color(0xFFE11D48), texto = "Día Libre")
                LeyendaIndicador(color = LinceTextSecondary.copy(alpha = 0.4f), texto = "Pasado")
            }

            if (mensajeAlerta != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = LinceErrorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mensajeAlerta!!,
                        color = LinceError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (mostrarDialogoBloqueo && fechaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBloqueo = false },
            containerColor = LinceSurface,
            title = {
                Text("Solicitar Día Libre", color = LinceTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "¿Confirmas que deseas bloquear el día ${fechaSeleccionada}? Operaciones no podrá asignarte servicios en esta fecha.",
                    color = LinceTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        diasBloqueados = diasBloqueados + fechaSeleccionada!!
                        mostrarDialogoBloqueo = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LinceCyan)
                ) {
                    Text("Bloquear Día", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBloqueo = false }) {
                    Text("Cancelar", color = LinceTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DiaCalendarioItem(
    day: CalendarDay,
    hoy: LocalDate,
    esSeleccionado: Boolean,
    estaBloqueado: Boolean,
    tieneServicio: Boolean,
    onClick: (LocalDate) -> Unit
) {
    if (day.position == DayPosition.MonthDate) {
        val esHoy = day.date == hoy
        val esPasado = day.date.isBefore(hoy)

        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(3.dp)
                .clip(CircleShape)
                .background(
                    when {
                        esSeleccionado -> LinceCyan
                        estaBloqueado -> Color(0xFFE11D48)
                        esHoy -> LinceSurfaceBorder
                        else -> Color.Transparent
                    }
                )
                .clickable { onClick(day.date) },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    fontSize = 13.sp,
                    fontWeight = if (esHoy || esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        esSeleccionado -> Color.White
                        esPasado -> LinceTextSecondary.copy(alpha = 0.35f)
                        estaBloqueado -> Color.White
                        else -> LinceTextPrimary
                    }
                )

                if (tieneServicio && !esSeleccionado) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(LinceCyan)
                    )
                }
            }
        }
    } else {
        Spacer(modifier = Modifier.aspectRatio(1f))
    }
}

@Composable
private fun LeyendaIndicador(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = texto, color = LinceTextSecondary, fontSize = 11.sp)
    }
}
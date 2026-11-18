package com.pminteractive.pmtours.ui.screens.coordinador

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
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.ui.components.PanelBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.AsignacionCoordinador
import com.pminteractive.pmtours.viewmodel.CoordinadorViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class FiltroCoordinador(val etiqueta: String, val estados: Set<EstadoServicio>?) {
    TODOS("Todos", null),
    PENDIENTES("Pendiente", setOf(EstadoServicio.ASIGNADO)),
    CONFIRMADOS("Confirmado", setOf(EstadoServicio.CONFIRMADO)),
    RECHAZADOS("Rechazado", setOf(EstadoServicio.RECHAZADO))
}

@Composable
fun PanelCoordinadorScreen(
    usuario: Usuario?,
    onCambiarTab: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    viewModel: CoordinadorViewModel = viewModel()
) {
    LaunchedEffect(usuario?.sede) {
        usuario?.sede?.let { viewModel.cargarServiciosDeSede(it) }
    }

    val asignaciones by viewModel.asignaciones.collectAsState()
    val resumen by viewModel.resumen.collectAsState()
    var filtroSeleccionado by remember { mutableStateOf(FiltroCoordinador.TODOS) }

    val asignacionesFiltradas = remember(asignaciones, filtroSeleccionado) {
        val estados = filtroSeleccionado.estados
        if (estados == null) {
            asignaciones
        } else {
            asignaciones.filter { it.estado in estados }
        }
    }

    val fechaCabecera = remember {
        try {
            val hoy = LocalDate.now()
            val formato = DateTimeFormatter.ofPattern(
                "EEEE d 'de' MMMM",
                Locale("es", "CL")
            )
            hoy.format(formato).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "Hoy"
        }
    }

    val subtitulo = remember(usuario) {
        val nombre = usuario?.nombreCompleto ?: "Coordinación"
        val ciudad = usuario?.sede?.nombreCiudad ?: "Santiago"
        "$nombre · $ciudad"
    }

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            PanelBottomBar(
                rutaActual = Screen.PanelCoordinador.route,
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = fechaCabecera,
                color = LinceTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Panel de coordinación",
                    color = LinceTextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onCerrarSesion) {
                    Text(
                        text = "Salir",
                        color = LinceTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = subtitulo,
                color = LinceTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TarjetaKpi(
                    valor = resumen.pendientes,
                    etiqueta = "Pendientes",
                    color = LincePending,
                    modifier = Modifier.weight(1f)
                )
                TarjetaKpi(
                    valor = resumen.confirmados,
                    etiqueta = "Confirmados",
                    color = LinceSuccess,
                    modifier = Modifier.weight(1f)
                )
                TarjetaKpi(
                    valor = resumen.rechazados,
                    etiqueta = "Rechazados",
                    color = LinceDanger,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroCoordinador.values().forEach { filtro ->
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (asignacionesFiltradas.isEmpty()) {
                    item {
                        Surface(
                            color = LinceSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(LinceSurfaceBorder)
                            ),
                            modifier = Modifier.fillMaxWidth()
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
                                    text = "No hay asignaciones para este filtro en tu sede.",
                                    color = LinceTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(asignacionesFiltradas, key = { it.servicioId }) { asignacion ->
                        FilaAsignacion(asignacion)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaKpi(
    valor: Int,
    etiqueta: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = valor.toString(),
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = etiqueta,
                color = LinceTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FilaAsignacion(asignacion: AsignacionCoordinador) {
    val estilo = asignacion.estado.estiloVisual()

    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asignacion.nombreOperador,
                    color = LinceTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${asignacion.hora} · ${asignacion.tipo}",
                    color = LinceTextSecondary,
                    fontSize = 12.sp
                )
            }

            Surface(
                color = estilo.fondo,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = estilo.etiqueta,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    color = estilo.texto,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

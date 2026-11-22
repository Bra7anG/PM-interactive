package com.pminteractive.pmtours.ui.screens.coordinador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.network.NetworkMonitor
import com.pminteractive.pmtours.network.dto.GuiaDto
import com.pminteractive.pmtours.network.dto.TransportistaDto
import com.pminteractive.pmtours.ui.components.AdminBottomBar
import com.pminteractive.pmtours.ui.components.CoordinadorBottomBar
import com.pminteractive.pmtours.ui.components.EstadoServicioBadge
import com.pminteractive.pmtours.ui.components.ServicioCoordinadorCard
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.CoordinadorViewModel
import com.pminteractive.pmtours.viewmodel.SeccionCoordinador
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class FiltroEstadoLocal(val etiqueta: String, val estado: EstadoServicio?) {
    TODOS("Todos", null),
    PENDIENTES("Pendientes", EstadoServicio.ASIGNADO),
    CONFIRMADOS("Confirmados", EstadoServicio.CONFIRMADO),
    RECHAZADOS("Rechazados", EstadoServicio.RECHAZADO),
    CANCELADOS("Cancelados", EstadoServicio.CANCELADO),
    EN_RUTA("En ruta", EstadoServicio.EN_RUTA),
    FINALIZADOS("Finalizados", EstadoServicio.FINALIZADO)
}

private enum class OrdenPersonal(val etiqueta: String) {
    EVALUACION("★ Mejor evaluación"),
    NOMBRE("Nombre (A-Z)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelCoordinadorScreen(
    usuario: Usuario?,
    onCerrarSesion: () -> Unit = {},
    onCambiarTab: (String) -> Unit = {},
    viewModel: CoordinadorViewModel = viewModel(),
    esModoAdmin: Boolean = false
) {
    LaunchedEffect(usuario?.sede) {
        viewModel.inicializar(usuario?.sede ?: Sede.SANTIAGO)
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
            viewModel.recargarTodo()
        }
        estabaConectado = estaConectado
    }

    val sedeSeleccionada by viewModel.sedeSeleccionada.collectAsState()
    val fechaSeleccionada by viewModel.fechaSeleccionada.collectAsState()
    val seccionActiva by viewModel.seccionActiva.collectAsState()
    val queryBusqueda by viewModel.queryBusqueda.collectAsState()
    val servicios by viewModel.servicios.collectAsState()
    val transportistas by viewModel.transportistas.collectAsState()
    val guias by viewModel.guias.collectAsState()
    val nombresTrabajadores by viewModel.nombresTrabajadores.collectAsState()
    val servicioDetalle by viewModel.servicioDetalle.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val errorMensaje by viewModel.errorMensaje.collectAsState()

    var filtroEstado by remember { mutableStateOf(FiltroEstadoLocal.TODOS) }
    var ordenPersonal by remember { mutableStateOf(OrdenPersonal.EVALUACION) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    // Conteos por estado para la barra unificada (Opción A)
    val conteosPorEstado = remember(servicios) {
        servicios.groupingBy { it.estado }.eachCount()
    }

    val serviciosFiltrados = remember(servicios, filtroEstado) {
        if (filtroEstado.estado == null) {
            servicios
        } else {
            servicios.filter { it.estado == filtroEstado.estado }
        }
    }

    val transportistasOrdenados = remember(transportistas, ordenPersonal) {
        when (ordenPersonal) {
            OrdenPersonal.EVALUACION -> transportistas.sortedByDescending { it.evaluacionPromedio }
            OrdenPersonal.NOMBRE -> transportistas.sortedBy { it.nombre.lowercase() }
        }
    }

    val guiasOrdenados = remember(guias, ordenPersonal) {
        when (ordenPersonal) {
            OrdenPersonal.EVALUACION -> guias.sortedByDescending { it.evaluacionPromedio }
            OrdenPersonal.NOMBRE -> guias.sortedBy { it.nombre.lowercase() }
        }
    }

    // Selector de calendario mensual ilimitado
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaSeleccionada
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val nuevaFecha = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        viewModel.seleccionarFecha(nuevaFecha)
                    }
                    mostrarDatePicker = false
                }) {
                    Text("Aceptar", color = LinceCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar", color = LinceTextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = LinceSurface)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = LinceSurface,
                    titleContentColor = LinceTextPrimary,
                    headlineContentColor = LinceCyan,
                    weekdayContentColor = LinceTextSecondary,
                    dayContentColor = LinceTextPrimary,
                    selectedDayContainerColor = LinceCyan,
                    selectedDayContentColor = LinceBackground,
                    todayContentColor = LinceCyan,
                    todayDateBorderColor = LinceCyan
                )
            )
        }
    }

    // Ficha de detalle al tocar un servicio (con opción de Cancelar)
    servicioDetalle?.let { servicio ->
        val codigoResponsable = servicio.driverId ?: servicio.guiaId
        val nombreResponsable = codigoResponsable?.let { nombresTrabajadores[it] ?: it }

        DetalleServicioCoordinadorSheet(
            servicio = servicio,
            nombreResponsable = nombreResponsable,
            onDismiss = { viewModel.cerrarDetalleServicio() },
            onCancelarServicio = { viewModel.cancelarServicio(servicio.id) }
        )
    }

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            if (esModoAdmin) {
                AdminBottomBar(
                    rutaActual = Screen.ServiciosAdministrador.route,
                    onNavegar = onCambiarTab
                )
            } else {
                CoordinadorBottomBar(
                    rutaActual = Screen.PanelCoordinador.route,
                    onNavegar = onCambiarTab
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // 1. Encabezado limpio + indicador offline
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if(esModoAdmin) "Servicios Operativos" else "Coordinación",
                            color = LinceTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = usuario?.nombreCompleto ?: "Operaciones PM Tours",
                            color = LinceTextSecondary,
                            fontSize = 13.sp
                        )
                        if (!estaConectado) {
                            Spacer(modifier = Modifier.height(4.dp))
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
                                    text = "Sin conexión · Datos locales",
                                    color = LincePending,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    TextButton(onClick = { viewModel.recargarTodo() }) {
                        Text(
                            text = "Actualizar",
                            color = LinceCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 2. Selectores compactos lado a lado: [Servicios ▾]   [Santiago ▾]
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SelectorCompactoDropdown(
                        etiquetaActual = seccionActiva.titulo,
                        opciones = SeccionCoordinador.values().toList(),
                        obtenerTexto = { it.titulo },
                        onSeleccionar = { viewModel.seleccionarSeccion(it) },
                        destacar = true,
                        modifier = Modifier.weight(1f)
                    )

                    SelectorCompactoDropdown(
                        etiquetaActual = sedeSeleccionada.nombreCiudad,
                        opciones = Sede.values().toList(),
                        obtenerTexto = { it.nombreCiudad },
                        onSeleccionar = { viewModel.seleccionarSede(it) },
                        destacar = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Controles contextuales según la vista activa
            if (seccionActiva == SeccionCoordinador.SERVICIOS) {
                // Selector de fecha con calendario ilimitado (< Fecha ▾ >)
                item {
                    BarraSelectorFecha(
                        fecha = fechaSeleccionada,
                        onDiaAnterior = { viewModel.seleccionarFecha(fechaSeleccionada.minusDays(1)) },
                        onDiaSiguiente = { viewModel.seleccionarFecha(fechaSeleccionada.plusDays(1)) },
                        onAbrirCalendario = { mostrarDatePicker = true }
                    )
                }

                // Opción A: Barra única de KPIs + Filtros con contador integrado
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(FiltroEstadoLocal.values()) { filtro ->
                            val activo = filtro == filtroEstado
                            val cantidad = if (filtro.estado == null) {
                                servicios.size
                            } else {
                                conteosPorEstado[filtro.estado] ?: 0
                            }

                            FilterChip(
                                selected = activo,
                                onClick = { filtroEstado = filtro },
                                label = {
                                    Text(
                                        text = "${filtro.etiqueta} ($cantidad)",
                                        fontSize = 12.sp,
                                        fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = LinceSurface,
                                    labelColor = LinceTextSecondary,
                                    selectedContainerColor = LinceCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = LinceCyan
                                )
                            )
                        }
                    }
                }
            } else {
                // Buscador + ordenamiento por estrellas (★) o nombre
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = queryBusqueda,
                            onValueChange = { viewModel.actualizarBusqueda(it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            placeholder = {
                                Text(
                                    text = if (seccionActiva == SeccionCoordinador.TRANSPORTISTAS) {
                                        "Buscar transportista por nombre o código..."
                                    } else {
                                        "Buscar guía por nombre o código..."
                                    },
                                    color = LinceTextSecondary,
                                    fontSize = 13.sp
                                )
                            },
                            trailingIcon = {
                                if (queryBusqueda.isNotEmpty()) {
                                    TextButton(onClick = { viewModel.actualizarBusqueda("") }) {
                                        Text("Limpiar", color = LinceCyan, fontSize = 12.sp)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LinceCyan,
                                unfocusedBorderColor = LinceSurfaceBorder,
                                focusedTextColor = LinceTextPrimary,
                                unfocusedTextColor = LinceTextPrimary,
                                focusedContainerColor = LinceSurface,
                                unfocusedContainerColor = LinceSurface
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val totalItems = if (seccionActiva == SeccionCoordinador.TRANSPORTISTAS) {
                                transportistasOrdenados.size
                            } else {
                                guiasOrdenados.size
                            }
                            Text(
                                text = "$totalItems disponibles",
                                color = LinceTextSecondary,
                                fontSize = 12.sp
                            )

                            Surface(
                                color = LinceSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = SolidColor(LinceSurfaceBorder)
                                ),
                                modifier = Modifier.clickable {
                                    ordenPersonal = if (ordenPersonal == OrdenPersonal.EVALUACION) {
                                        OrdenPersonal.NOMBRE
                                    } else {
                                        OrdenPersonal.EVALUACION
                                    }
                                }
                            ) {
                                Text(
                                    text = "Orden: ${ordenPersonal.etiqueta}",
                                    color = LinceCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Listado principal
            if (cargando) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = LinceCyan)
                    }
                }
            } else if (errorMensaje != null) {
                item {
                    Surface(
                        color = LinceSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(LinceError)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMensaje ?: "Error de conexión",
                                color = LinceError,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { viewModel.recargarTodo() }) {
                                Text("Reintentar", color = LinceCyan)
                            }
                        }
                    }
                }
            } else {
                when (seccionActiva) {
                    SeccionCoordinador.SERVICIOS -> {
                        if (serviciosFiltrados.isEmpty()) {
                            item {
                                MensajeListaVacia("No hay servicios en esta categoría para ${sedeSeleccionada.nombreCiudad}.")
                            }
                        } else {
                            items(serviciosFiltrados, key = { it.id }) { servicio ->
                                val codigoWorker = servicio.driverId ?: servicio.guiaId
                                val nombreHumano = codigoWorker?.let { nombresTrabajadores[it] }

                                ServicioCoordinadorCard(
                                    servicio = servicio,
                                    nombreAsignado = nombreHumano,
                                    onCardClick = { viewModel.abrirDetalleServicio(servicio) }
                                )
                            }
                        }
                    }

                    SeccionCoordinador.TRANSPORTISTAS -> {
                        if (transportistasOrdenados.isEmpty()) {
                            item {
                                MensajeListaVacia("No se encontraron transportistas en ${sedeSeleccionada.nombreCiudad}.")
                            }
                        } else {
                            items(transportistasOrdenados, key = { it.id }) { transportista ->
                                TarjetaTransportistaSegura(transportista = transportista)
                            }
                        }
                    }

                    SeccionCoordinador.GUIAS -> {
                        if (guiasOrdenados.isEmpty()) {
                            item {
                                MensajeListaVacia("No se encontraron guías en ${sedeSeleccionada.nombreCiudad}.")
                            }
                        } else {
                            items(guiasOrdenados, key = { it.id }) { guia ->
                                TarjetaGuiaSegura(guia = guia)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> SelectorCompactoDropdown(
    etiquetaActual: String,
    opciones: List<T>,
    obtenerTexto: (T) -> String,
    onSeleccionar: (T) -> Unit,
    destacar: Boolean,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (destacar) LinceCyan.copy(alpha = 0.14f) else LinceSurface)
                .border(
                    width = 1.dp,
                    color = if (destacar) LinceCyan else LinceSurfaceBorder,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { expandido = true }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = etiquetaActual,
                color = if (destacar) LinceCyan else LinceTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "▾",
                color = if (destacar) LinceCyan else LinceTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
            modifier = Modifier
                .background(LinceSurface)
                .border(1.dp, LinceSurfaceBorder)
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = obtenerTexto(opcion),
                            color = LinceTextPrimary,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onSeleccionar(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BarraSelectorFecha(
    fecha: LocalDate,
    onDiaAnterior: () -> Unit,
    onDiaSiguiente: () -> Unit,
    onAbrirCalendario: () -> Unit
) {
    val localeEs = Locale("es", "CL")
    val textoFecha = remember(fecha) {
        val hoy = LocalDate.now()
        val base = fecha.format(DateTimeFormatter.ofPattern("EEE d 'de' MMM yyyy", localeEs))
            .replaceFirstChar { it.uppercase() }
        when (fecha) {
            hoy -> "Hoy · $base"
            hoy.plusDays(1) -> "Mañana · $base"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalleServicioCoordinadorSheet(
    servicio: Servicio,
    nombreResponsable: String?,
    onDismiss: () -> Unit,
    onCancelarServicio: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confirmarCancelacion by remember { mutableStateOf(false) }

    val etiquetaTipo = when (servicio.tipo) {
        TipoServicio.TRANSFER -> "Transfer"
        TipoServicio.GUIDED_SERVICE -> "Servicio Guiado"
        TipoServicio.EXCURSION -> "Excursión"
    }

    val puedeCancelar = servicio.estado != EstadoServicio.FINALIZADO &&
            servicio.estado != EstadoServicio.CANCELADO

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LinceSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$etiquetaTipo · ${servicio.numeroReserva}",
                    color = LinceCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                EstadoServicioBadge(estado = servicio.estado)
            }

            Text(
                text = servicio.titulo,
                color = LinceTextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                color = LinceBackground,
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilaDetalle("Fecha y Horario", "${servicio.fecha} · ${servicio.horaInicio} a ${servicio.horaFinEstimada}")
                    FilaDetalle("Origen (Pickup)", servicio.lugarPickup)
                    FilaDetalle("Destino (Drop-off)", servicio.lugarDropOff)
                    FilaDetalle("Pasajeros", "${servicio.cantidadPasajeros} PAX (${servicio.paxTexto})")
                    FilaDetalle(
                        etiqueta = if (!servicio.driverId.isNullOrBlank()) "Transportista" else "Guía",
                        valor = nombreResponsable ?: "Sin asignar"
                    )
                    if (!servicio.vehiculoId.isNullOrBlank()) {
                        FilaDetalle("Vehículo asignado", servicio.vehiculoId)
                    }
                }
            }

            if (servicio.notasAgencia.isNotBlank() || servicio.detalleItinerario.isNotBlank()) {
                Surface(
                    color = LinceBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(LinceSurfaceBorder)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Notas e Itinerario",
                            color = LinceCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (servicio.detalleItinerario.isNotBlank()) {
                            Text(servicio.detalleItinerario, color = LinceTextPrimary, fontSize = 13.sp)
                        }
                        if (servicio.notasAgencia.isNotBlank()) {
                            Text(servicio.notasAgencia, color = LinceTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (servicio.checkIn != null || servicio.checkOut != null) {
                Surface(
                    color = LinceBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(LinceSuccess.copy(alpha = 0.5f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Trazabilidad Operativa",
                            color = LinceSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        servicio.checkIn?.let { hito ->
                            val hora = Instant.ofEpochMilli(hito.timestamp)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("HH:mm"))
                            Text(
                                text = "Check-in registrado a las $hora",
                                color = LinceTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                        servicio.checkOut?.let { hito ->
                            val hora = Instant.ofEpochMilli(hito.timestamp)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("HH:mm"))
                            Text(
                                text = "Check-out finalizado a las $hora",
                                color = LinceTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            if (puedeCancelar) {
                Spacer(modifier = Modifier.height(4.dp))
                if (!confirmarCancelacion) {
                    OutlinedButton(
                        onClick = { confirmarCancelacion = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceError),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(LinceError)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Cancelar Servicio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { confirmarCancelacion = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Volver", color = LinceTextSecondary)
                        }
                        Button(
                            onClick = {
                                onCancelarServicio()
                                confirmarCancelacion = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LinceError,
                                contentColor = LinceTextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Confirmar Cancelación", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaDetalle(etiqueta: String, valor: String) {
    Column {
        Text(text = etiqueta, color = LinceTextSecondary, fontSize = 11.sp)
        Text(text = valor, color = LinceTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TarjetaTransportistaSegura(transportista: TransportistaDto) {
    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transportista.nombre,
                        color = LinceTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${transportista.codigo} · Base ${transportista.oficina.nombre}",
                        color = LinceTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "★ ${String.format(Locale.US, "%.1f", transportista.evaluacionPromedio)}",
                    color = LinceCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (transportista.vehiculos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                transportista.vehiculos.forEach { vehiculo ->
                    val tipoLegible = vehiculo.tipo.lowercase().replaceFirstChar { it.uppercase() }
                    Text(
                        text = "• $tipoLegible (${vehiculo.patente}) · Cap. ${vehiculo.capacidad} pax",
                        color = LinceTextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaGuiaSegura(guia: GuiaDto) {
    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guia.nombre,
                        color = LinceTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${guia.codigo} · Base ${guia.oficina.nombre}",
                        color = LinceTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "★ ${String.format(Locale.US, "%.1f", guia.evaluacionPromedio)}",
                    color = LinceCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (guia.idiomas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val idiomasLegibles = guia.idiomas.joinToString(" · ") { it.nombre }
                Text(
                    text = "Idiomas: $idiomasLegibles",
                    color = LinceCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MensajeListaVacia(texto: String) {
    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = texto,
                color = LinceTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}
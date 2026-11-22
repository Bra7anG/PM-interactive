package com.pminteractive.pmtours.ui.screens.coordinador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.ui.components.CoordinadorBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.CrearServicioViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearServicioScreen(
    usuario: Usuario?,
    onCambiarTab: (String) -> Unit = {},
    onServicioCreado: () -> Unit = {},
    viewModel: CrearServicioViewModel = viewModel(),
    esModoAdminIncrustado: Boolean = false // <--- NUEVO PARÁMETRO
) {
    val sedeDefault = usuario?.sede ?: Sede.SANTIAGO

    LaunchedEffect(sedeDefault) {
        viewModel.inicializar(sedeDefault)
    }

    val uiState by viewModel.uiState.collectAsState()
    var mostrarDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.servicioCreadoExito) {
        if (uiState.servicioCreadoExito) {
            viewModel.reiniciarFormulario(sedeDefault)
            onServicioCreado()
        }
    }

    // Calendario mensual completo sin límite de días hacia el futuro
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.fecha
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

    // SI ESTÁ INCRUSTADO EN EL ADMIN: Solo dibujamos el contenido sin el Scaffold (para evitar 2 barras).
    if (esModoAdminIncrustado) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ContenidoCrearServicio(
                usuario = usuario,
                uiState = uiState,
                viewModel = viewModel,
                mostrarDatePicker = { mostrarDatePicker = it },
                innerPadding = PaddingValues(0.dp) // Sin padding extra del Scaffold
            )
        }
    } else {
        // SI ENTRÓ EL COORDINADOR NORMAL: Usamos el Scaffold con su barra inferior de siempre.
        Scaffold(
            containerColor = LinceBackground,
            bottomBar = {
                CoordinadorBottomBar(
                    rutaActual = Screen.CrearServicioCoordinador.route,
                    onNavegar = onCambiarTab
                )
            }
        ) { innerPadding ->
            ContenidoCrearServicio(
                usuario = usuario,
                uiState = uiState,
                viewModel = viewModel,
                mostrarDatePicker = { mostrarDatePicker = it },
                innerPadding = innerPadding
            )
        }
    }
}

// EXTRAJIMOS TODO EL CONTENIDO A UNA FUNCIÓN REUTILIZABLE PARA NO DUPLICAR CÓDIGO
@Composable
private fun ContenidoCrearServicio(
    usuario: Usuario?,
    uiState: com.pminteractive.pmtours.viewmodel.CrearServicioUiState,
    viewModel: CrearServicioViewModel,
    mostrarDatePicker: (Boolean) -> Unit,
    innerPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp)
    ) {
        // 1. Encabezado
        item {
            Column {
                Text(
                    text = "Crear Servicio",
                    color = LinceTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Asignación operativa · ${usuario?.nombreCompleto ?: "Coordinador"}",
                    color = LinceTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // 2. Sede y Tipo de Servicio
        item {
            SeccionFormulario(titulo = "1. Sede y Tipo de Servicio") {
                Text(
                    text = "Sede operativa",
                    color = LinceTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Sede.values().forEach { sede ->
                        val seleccionada = sede == uiState.sedeSeleccionada
                        FilterChip(
                            selected = seleccionada,
                            onClick = { viewModel.seleccionarSede(sede) },
                            label = {
                                Text(
                                    text = sede.nombreCiudad,
                                    fontSize = 11.sp,
                                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = LinceBackground,
                                labelColor = LinceTextSecondary,
                                selectedContainerColor = LinceCyan.copy(alpha = 0.2f),
                                selectedLabelColor = LinceCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Modalidad del servicio",
                    color = LinceTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipoServicio.values().forEach { tipo ->
                        val activo = tipo == uiState.tipoServicio
                        val etiqueta = when (tipo) {
                            TipoServicio.TRANSFER -> "Transfer"
                            TipoServicio.EXCURSION -> "Excursión"
                            TipoServicio.GUIDED_SERVICE -> "Guiado"
                        }
                        FilterChip(
                            selected = activo,
                            onClick = { viewModel.seleccionarTipoServicio(tipo) },
                            label = {
                                Text(
                                    text = etiqueta,
                                    fontSize = 12.sp,
                                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = LinceBackground,
                                labelColor = LinceTextSecondary,
                                selectedContainerColor = LinceCyan.copy(alpha = 0.2f),
                                selectedLabelColor = LinceCyan
                            )
                        )
                    }
                }
            }
        }

        // 3. Reserva y Agencia / Cliente
        item {
            SeccionFormulario(titulo = "2. Reserva y Cliente") {
                CampoTextoLince(
                    valor = uiState.numeroReserva,
                    onValorCambio = { viewModel.actualizarNumeroReserva(it) },
                    etiqueta = "Código de Reserva *",
                    placeholder = "Ej: WHFT223423"
                )

                Spacer(modifier = Modifier.height(10.dp))

                CampoTextoLince(
                    valor = uiState.titulo,
                    onValorCambio = { viewModel.actualizarTitulo(it) },
                    etiqueta = "Título o Descripción *",
                    placeholder = "Ej: Transfer IN Aeropuerto SCL - Hotel"
                )

                Spacer(modifier = Modifier.height(10.dp))

                SelectorDesplegableLince(
                    etiqueta = "Agencia / Cliente (Opcional)",
                    textoSeleccionado = uiState.clienteSeleccionado?.let {
                        "${it.nombre} (${it.pais})"
                    } ?: if (uiState.cargandoCatalogos) "Cargando clientes..." else "Seleccionar cliente...",
                    opciones = listOf(null) + uiState.clientes,
                    obtenerTextoOpcion = { cliente ->
                        cliente?.let { "${it.nombre} (${it.pais})" } ?: "Sin cliente específico"
                    },
                    onSeleccionar = { viewModel.seleccionarCliente(it) }
                )
            }
        }

        // 4. Fecha (Calendario Ilimitado), Horarios y Ruta
        item {
            SeccionFormulario(titulo = "3. Fecha, Horario y Ruta") {
                Text(
                    text = "Fecha del servicio *",
                    color = LinceTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                SelectorFechaIlimitada(
                    fecha = uiState.fecha,
                    onDiaAnterior = { viewModel.seleccionarFecha(uiState.fecha.minusDays(1)) },
                    onDiaSiguiente = { viewModel.seleccionarFecha(uiState.fecha.plusDays(1)) },
                    onAbrirCalendario = { mostrarDatePicker(true) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CampoTextoLince(
                        valor = uiState.horaInicio,
                        onValorCambio = { viewModel.actualizarHoraInicio(it) },
                        etiqueta = "Hora Pickup *",
                        placeholder = "09:00",
                        modifier = Modifier.weight(1f)
                    )
                    CampoTextoLince(
                        valor = uiState.horaFinEstimada,
                        onValorCambio = { viewModel.actualizarHoraFin(it) },
                        etiqueta = "Hora Fin Est.",
                        placeholder = "11:00",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                CampoTextoLince(
                    valor = uiState.lugarPickup,
                    onValorCambio = { viewModel.actualizarPickup(it) },
                    etiqueta = "Punto de Origen (Pickup) *",
                    placeholder = "Ej: Aeropuerto Arturo Merino Benítez"
                )

                Spacer(modifier = Modifier.height(10.dp))

                CampoTextoLince(
                    valor = uiState.lugarDropOff,
                    onValorCambio = { viewModel.actualizarDropOff(it) },
                    etiqueta = "Punto de Destino (Drop-off) *",
                    placeholder = "Ej: Hotel Mandarin Oriental, Las Condes"
                )
            }
        }

        // 5. Pasajeros (PAX)
        item {
            SeccionFormulario(titulo = "4. Pasajeros (PAX)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ContadorPax(
                        etiqueta = "Adultos",
                        valor = uiState.adultos,
                        onDecrementar = { viewModel.actualizarAdultos(uiState.adultos - 1) },
                        onIncrementar = { viewModel.actualizarAdultos(uiState.adultos + 1) },
                        modifier = Modifier.weight(1f)
                    )
                    ContadorPax(
                        etiqueta = "Niños",
                        valor = uiState.ninos,
                        onDecrementar = { viewModel.actualizarNinos(uiState.ninos - 1) },
                        onIncrementar = { viewModel.actualizarNinos(uiState.ninos + 1) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Total: ${uiState.adultos + uiState.ninos} pasajeros (${uiState.adultos}+${uiState.ninos})",
                    color = LinceCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 6. Asignación Operativa con revisión de días bloqueados
        item {
            SeccionFormulario(titulo = "5. Asignación de Personal") {
                if (uiState.cargandoStaff) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = LinceCyan,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Cargando staff y disponibilidad...",
                            color = LinceTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else if (uiState.esServicioTransporte) {
                    SelectorDesplegableLince(
                        etiqueta = "Transportista (${uiState.sedeSeleccionada.nombreCiudad}) *",
                        textoSeleccionado = uiState.transportistaSeleccionado?.let { t ->
                            val aviso = if (uiState.estaBloqueadoEnFecha(t.codigo)) " · 🚫 Bloqueado" else ""
                            "${t.nombre} (${t.codigo})$aviso"
                        } ?: "Seleccionar transportista...",
                        opciones = uiState.transportistas,
                        obtenerTextoOpcion = { t ->
                            when {
                                uiState.estaBloqueadoEnFecha(t.codigo) ->
                                    "${t.nombre} (${t.codigo}) · 🚫 Día bloqueado"
                                uiState.tieneViajesEnFecha(t.codigo) ->
                                    "${t.nombre} (${t.codigo}) · ${t.vehiculos.size} veh. (Con viajes)"
                                else ->
                                    "${t.nombre} (${t.codigo}) · ${t.vehiculos.size} veh."
                            }
                        },
                        onSeleccionar = { viewModel.seleccionarTransportista(it) }
                    )

                    if (uiState.trabajadorSeleccionadoBloqueado) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Este transportista tiene bloqueado el día seleccionado en su calendario.",
                            color = LinceError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val vehiculosFlota = uiState.transportistaSeleccionado?.vehiculos ?: emptyList()
                    SelectorDesplegableLince(
                        etiqueta = "Vehículo de Flota *",
                        textoSeleccionado = uiState.vehiculoSeleccionado?.let { v ->
                            val tipoLegible = v.tipo.lowercase().replaceFirstChar { it.uppercase() }
                            "$tipoLegible · ${v.patente} (Cap. ${v.capacidad} pax)"
                        } ?: if (uiState.transportistaSeleccionado == null) {
                            "Primero selecciona un transportista"
                        } else {
                            "Seleccionar vehículo..."
                        },
                        opciones = vehiculosFlota,
                        obtenerTextoOpcion = { v ->
                            val tipoLegible = v.tipo.lowercase().replaceFirstChar { it.uppercase() }
                            "$tipoLegible · ${v.patente} (Cap. ${v.capacidad} pax)"
                        },
                        onSeleccionar = { viewModel.seleccionarVehiculo(it) }
                    )
                } else {
                    SelectorDesplegableLince(
                        etiqueta = "Idioma Requerido (Filtro)",
                        textoSeleccionado = uiState.idiomaSeleccionado?.nombre ?: "Todos los idiomas",
                        opciones = listOf(null) + uiState.idiomas,
                        obtenerTextoOpcion = { idioma ->
                            idioma?.nombre ?: "Todos los idiomas"
                        },
                        onSeleccionar = { viewModel.seleccionarIdioma(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SelectorDesplegableLince(
                        etiqueta = "Guía Turístico (${uiState.sedeSeleccionada.nombreCiudad}) *",
                        textoSeleccionado = uiState.guiaSeleccionado?.let { g ->
                            val aviso = if (uiState.estaBloqueadoEnFecha(g.codigo)) " · 🚫 Bloqueado" else ""
                            "${g.nombre} (${g.codigo})$aviso"
                        } ?: "Seleccionar guía (${uiState.guiasDisponiblesParaIdioma.size} disp.)...",
                        opciones = uiState.guiasDisponiblesParaIdioma,
                        obtenerTextoOpcion = { g ->
                            val idiomasTexto = g.idiomas.joinToString(", ") { it.nombre }
                            when {
                                uiState.estaBloqueadoEnFecha(g.codigo) ->
                                    "${g.nombre} · 🚫 Día bloqueado"
                                uiState.tieneViajesEnFecha(g.codigo) ->
                                    "${g.nombre} · $idiomasTexto (Con viajes)"
                                else ->
                                    "${g.nombre} · $idiomasTexto"
                            }
                        },
                        onSeleccionar = { viewModel.seleccionarGuia(it) }
                    )

                    if (uiState.trabajadorSeleccionadoBloqueado) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Este guía tiene bloqueado el día seleccionado en su calendario.",
                            color = LinceError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 7. Notas Operativas
        item {
            SeccionFormulario(titulo = "6. Notas Operativas (Opcional)") {
                CampoTextoLince(
                    valor = uiState.notasAgencia,
                    onValorCambio = { viewModel.actualizarNotas(it) },
                    etiqueta = "Instrucciones para el operador",
                    placeholder = "Ej: Vuelo LA501, cartel con nombre del pasajero..."
                )
            }
        }

        // 8. Mensaje de error y Botón Guardar
        item {
            if (uiState.errorMensaje != null) {
                Surface(
                    color = LinceError.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(LinceError)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Text(
                        text = uiState.errorMensaje ?: "",
                        color = LinceError,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = { viewModel.guardarServicio() },
                enabled = !uiState.guardando,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LinceCyan,
                    contentColor = LinceBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(
                        color = LinceBackground,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Crear y Asignar Servicio",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectorFechaIlimitada(
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LinceBackground)
            .border(1.dp, LinceSurfaceBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
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
                .padding(horizontal = 10.dp, vertical = 8.dp),
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

@Composable
private fun SeccionFormulario(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = titulo,
                color = LinceCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            contenido()
        }
    }
}

@Composable
private fun CampoTextoLince(
    valor: String,
    onValorCambio: (String) -> Unit,
    etiqueta: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        Text(
            text = etiqueta,
            color = LinceTextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValorCambio,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = {
                Text(
                    text = placeholder,
                    color = LinceTextSecondary.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LinceCyan,
                unfocusedBorderColor = LinceSurfaceBorder,
                focusedTextColor = LinceTextPrimary,
                unfocusedTextColor = LinceTextPrimary,
                focusedContainerColor = LinceBackground,
                unfocusedContainerColor = LinceBackground
            )
        )
    }
}

@Composable
private fun <T> SelectorDesplegableLince(
    etiqueta: String,
    textoSeleccionado: String,
    opciones: List<T>,
    obtenerTextoOpcion: (T) -> String,
    onSeleccionar: (T) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            color = LinceTextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LinceBackground)
                    .border(
                        width = 1.dp,
                        color = if (expandido) LinceCyan else LinceSurfaceBorder,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = opciones.isNotEmpty()) { expandido = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = textoSeleccionado,
                    color = LinceTextPrimary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "▼",
                    color = LinceCyan,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 8.dp)
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
                                text = obtenerTextoOpcion(opcion),
                                color = LinceTextPrimary,
                                fontSize = 13.sp
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
}

@Composable
private fun ContadorPax(
    etiqueta: String,
    valor: Int,
    onDecrementar: () -> Unit,
    onIncrementar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = etiqueta,
            color = LinceTextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(LinceBackground)
                .border(1.dp, LinceSurfaceBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(LinceSurface)
                    .clickable { onDecrementar() },
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = LinceCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Text(
                text = valor.toString(),
                color = LinceTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(LinceSurface)
                    .clickable { onIncrementar() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = LinceCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
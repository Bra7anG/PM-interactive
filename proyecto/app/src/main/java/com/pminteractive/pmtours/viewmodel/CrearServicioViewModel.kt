package com.pminteractive.pmtours.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.network.dto.ClienteDto
import com.pminteractive.pmtours.network.dto.GuiaDto
import com.pminteractive.pmtours.network.dto.IdiomaDto
import com.pminteractive.pmtours.network.dto.OficinaDto
import com.pminteractive.pmtours.network.dto.TransportistaDto
import com.pminteractive.pmtours.network.dto.VehiculoDto
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.repository.CoordinadorRepository
import com.pminteractive.pmtours.repository.DisponibilidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

typealias MapaDiasBloqueados = Map<String, Set<LocalDate>>

data class CrearServicioUiState(
    // Listas cargadas desde la API
    val oficinas: List<OficinaDto> = emptyList(),
    val idiomas: List<IdiomaDto> = emptyList(),
    val clientes: List<ClienteDto> = emptyList(),
    val transportistas: List<TransportistaDto> = emptyList(),
    val guias: List<GuiaDto> = emptyList(),

    // Disponibilidad y ocupación en la fecha elegida
    val bloqueosPorTrabajador: MapaDiasBloqueados = emptyMap(),
    val trabajadoresConViajesEseDia: Set<String> = emptySet(),

    // Campos del formulario
    val numeroReserva: String = "",
    val titulo: String = "",
    val tipoServicio: TipoServicio = TipoServicio.TRANSFER,
    val sedeSeleccionada: Sede = Sede.SANTIAGO,
    val clienteSeleccionado: ClienteDto? = null,
    val idiomaSeleccionado: IdiomaDto? = null,
    val fecha: LocalDate = LocalDate.now(),
    val horaInicio: String = "09:00",
    val horaFinEstimada: String = "11:00",
    val lugarPickup: String = "",
    val lugarDropOff: String = "",
    val adultos: Int = 2,
    val ninos: Int = 0,
    val transportistaSeleccionado: TransportistaDto? = null,
    val vehiculoSeleccionado: VehiculoDto? = null,
    val guiaSeleccionado: GuiaDto? = null,
    val tarifaTexto: String = "",
    val notasAgencia: String = "",

    // Estados de carga y validación
    val cargandoCatalogos: Boolean = false,
    val cargandoStaff: Boolean = false,
    val guardando: Boolean = false,
    val servicioCreadoExito: Boolean = false,
    val errorMensaje: String? = null
) {
    val esServicioTransporte: Boolean
        get() = tipoServicio == TipoServicio.TRANSFER || tipoServicio == TipoServicio.EXCURSION

    val guiasDisponiblesParaIdioma: List<GuiaDto>
        get() = if (idiomaSeleccionado == null) {
            guias
        } else {
            guias.filter { guia ->
                guia.idiomas.any { it.codigo == idiomaSeleccionado.codigo }
            }
        }

    fun estaBloqueadoEnFecha(codigoTrabajador: String): Boolean {
        return bloqueosPorTrabajador[codigoTrabajador]?.contains(fecha) == true
    }

    fun tieneViajesEnFecha(codigoTrabajador: String): Boolean {
        return trabajadoresConViajesEseDia.contains(codigoTrabajador)
    }

    val trabajadorSeleccionadoBloqueado: Boolean
        get() {
            val codigo = if (esServicioTransporte) {
                transportistaSeleccionado?.codigo
            } else {
                guiaSeleccionado?.codigo
            }
            return codigo != null && estaBloqueadoEnFecha(codigo)
        }
}

class CrearServicioViewModel(
    private val repository: CoordinadorRepository = AppContainer.coordinadorRepository,
    private val disponibilidadRepository: DisponibilidadRepository = AppContainer.disponibilidadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearServicioUiState())
    val uiState: StateFlow<CrearServicioUiState> = _uiState.asStateFlow()

    private var inicializado = false

    fun inicializar(sedePorDefecto: Sede) {
        if (!inicializado) {
            _uiState.update { it.copy(sedeSeleccionada = sedePorDefecto) }
            inicializado = true
            cargarCatalogosYClientes()
            cargarStaffYDisponibilidad(sedePorDefecto, _uiState.value.fecha)
        }
    }

    private fun cargarCatalogosYClientes() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoCatalogos = true, errorMensaje = null) }

            val catalogosRes = repository.obtenerCatalogos()
            val clientesRes = repository.obtenerClientes()

            _uiState.update { state ->
                state.copy(
                    oficinas = catalogosRes.getOrNull()?.oficinas ?: emptyList(),
                    idiomas = catalogosRes.getOrNull()?.idiomas ?: emptyList(),
                    clientes = clientesRes.getOrNull() ?: emptyList(),
                    cargandoCatalogos = false,
                    errorMensaje = if (catalogosRes.isFailure || clientesRes.isFailure) {
                        "No se pudieron cargar algunos catálogos desde la API."
                    } else null
                )
            }
        }
    }

    private fun cargarStaffYDisponibilidad(sede: Sede, fecha: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoStaff = true) }

            val transportistasLista = repository.obtenerTransportistas(oficinaCodigo = sede.codigo)
                .getOrDefault(emptyList())
            val guiasLista = repository.obtenerGuias(oficinaCodigo = sede.codigo)
                .getOrDefault(emptyList())

            // Consulta días bloqueados de cada trabajador en DisponibilidadRepository
            val todosLosCodigos = transportistasLista.map { it.codigo } + guiasLista.map { it.codigo }
            val mapaBloqueos = buildMap {
                todosLosCodigos.forEach { codigo ->
                    val dias = runCatching {
                        disponibilidadRepository.obtenerDiasBloqueados(codigo).first()
                    }.getOrDefault(emptySet())
                    if (dias.isNotEmpty()) {
                        put(codigo, dias)
                    }
                }
            }

            // Consulta qué trabajadores ya tienen viajes asignados ese día
            val ocupados = obtenerCodigosOcupadosEnFecha(fecha, sede)

            _uiState.update { state ->
                state.copy(
                    transportistas = transportistasLista,
                    guias = guiasLista,
                    bloqueosPorTrabajador = mapaBloqueos,
                    trabajadoresConViajesEseDia = ocupados,
                    cargandoStaff = false
                )
            }
        }
    }

    private fun actualizarOcupacionFecha(fecha: LocalDate, sede: Sede) {
        viewModelScope.launch {
            val ocupados = obtenerCodigosOcupadosEnFecha(fecha, sede)
            _uiState.update { it.copy(trabajadoresConViajesEseDia = ocupados) }
        }
    }

    private suspend fun obtenerCodigosOcupadosEnFecha(fecha: LocalDate, sede: Sede): Set<String> {
        val serviciosDelDia = repository.obtenerServiciosPorDiaYSede(fecha.toString(), sede.codigo)
            .getOrDefault(emptyList())

        return buildSet {
            serviciosDelDia.forEach { servicio ->
                if (servicio.estado != EstadoServicio.CANCELADO && servicio.estado != EstadoServicio.RECHAZADO) {
                    servicio.driverId?.takeIf { it.isNotBlank() }?.let { add(it) }
                    servicio.guiaId?.takeIf { it.isNotBlank() }?.let { add(it) }
                }
            }
        }
    }

    // Actualizaciones de campos
    fun actualizarNumeroReserva(valor: String) {
        _uiState.update { it.copy(numeroReserva = valor, errorMensaje = null) }
    }

    fun actualizarTitulo(valor: String) {
        _uiState.update { it.copy(titulo = valor, errorMensaje = null) }
    }

    fun seleccionarTipoServicio(tipo: TipoServicio) {
        _uiState.update {
            it.copy(
                tipoServicio = tipo,
                transportistaSeleccionado = null,
                vehiculoSeleccionado = null,
                guiaSeleccionado = null,
                idiomaSeleccionado = null,
                errorMensaje = null
            )
        }
    }

    fun seleccionarSede(sede: Sede) {
        if (_uiState.value.sedeSeleccionada == sede) return
        _uiState.update {
            it.copy(
                sedeSeleccionada = sede,
                transportistaSeleccionado = null,
                vehiculoSeleccionado = null,
                guiaSeleccionado = null,
                errorMensaje = null
            )
        }
        cargarStaffYDisponibilidad(sede, _uiState.value.fecha)
    }

    fun seleccionarCliente(cliente: ClienteDto?) {
        _uiState.update { it.copy(clienteSeleccionado = cliente, errorMensaje = null) }
    }

    fun seleccionarIdioma(idioma: IdiomaDto?) {
        _uiState.update { state ->
            val guiaValido = state.guiaSeleccionado?.takeIf { guia ->
                idioma == null || guia.idiomas.any { it.codigo == idioma.codigo }
            }
            state.copy(
                idiomaSeleccionado = idioma,
                guiaSeleccionado = guiaValido,
                errorMensaje = null
            )
        }
    }

    fun seleccionarFecha(fecha: LocalDate) {
        if (_uiState.value.fecha == fecha) return
        _uiState.update { it.copy(fecha = fecha, errorMensaje = null) }
        actualizarOcupacionFecha(fecha, _uiState.value.sedeSeleccionada)
    }

    fun actualizarHoraInicio(valor: String) {
        _uiState.update { it.copy(horaInicio = valor, errorMensaje = null) }
    }

    fun actualizarHoraFin(valor: String) {
        _uiState.update { it.copy(horaFinEstimada = valor, errorMensaje = null) }
    }

    fun actualizarPickup(valor: String) {
        _uiState.update { it.copy(lugarPickup = valor, errorMensaje = null) }
    }

    fun actualizarDropOff(valor: String) {
        _uiState.update { it.copy(lugarDropOff = valor, errorMensaje = null) }
    }

    fun actualizarAdultos(cantidad: Int) {
        _uiState.update { it.copy(adultos = cantidad.coerceAtLeast(1), errorMensaje = null) }
    }

    fun actualizarNinos(cantidad: Int) {
        _uiState.update { it.copy(ninos = cantidad.coerceAtLeast(0), errorMensaje = null) }
    }

    fun seleccionarTransportista(transportista: TransportistaDto?) {
        _uiState.update {
            it.copy(
                transportistaSeleccionado = transportista,
                vehiculoSeleccionado = transportista?.vehiculos?.firstOrNull(),
                errorMensaje = null
            )
        }
    }

    fun seleccionarVehiculo(vehiculo: VehiculoDto?) {
        _uiState.update { it.copy(vehiculoSeleccionado = vehiculo, errorMensaje = null) }
    }

    fun seleccionarGuia(guia: GuiaDto?) {
        _uiState.update { it.copy(guiaSeleccionado = guia, errorMensaje = null) }
    }

    fun actualizarTarifa(valor: String) {
        val soloNumeros = valor.filter { it.isDigit() }
        _uiState.update { it.copy(tarifaTexto = soloNumeros, errorMensaje = null) }
    }

    fun actualizarNotas(valor: String) {
        _uiState.update { it.copy(notasAgencia = valor, errorMensaje = null) }
    }

    fun guardarServicio() {
        val state = _uiState.value

        if (state.numeroReserva.isBlank()) {
            _uiState.update { it.copy(errorMensaje = "Ingresa el número de reserva.") }
            return
        }
        if (state.titulo.isBlank()) {
            _uiState.update { it.copy(errorMensaje = "Ingresa el título o descripción del servicio.") }
            return
        }
        if (state.lugarPickup.isBlank() || state.lugarDropOff.isBlank()) {
            _uiState.update { it.copy(errorMensaje = "Completa los puntos de pickup y destino.") }
            return
        }
        if (runCatching { LocalTime.parse(state.horaInicio.trim()) }.isFailure) {
            _uiState.update { it.copy(errorMensaje = "La hora de inicio debe tener formato HH:mm (ej. 09:30).") }
            return
        }

        if (state.esServicioTransporte) {
            if (state.transportistaSeleccionado == null) {
                _uiState.update { it.copy(errorMensaje = "Selecciona un transportista de ${state.sedeSeleccionada.nombreCiudad}.") }
                return
            }
            if (state.vehiculoSeleccionado == null) {
                _uiState.update { it.copy(errorMensaje = "Selecciona un vehículo de la flota del transportista.") }
                return
            }
        } else {
            if (state.guiaSeleccionado == null) {
                _uiState.update { it.copy(errorMensaje = "Selecciona un guía turístico de ${state.sedeSeleccionada.nombreCiudad}.") }
                return
            }
        }

        // Bloqueo estricto si el operador marcó ese día como no disponible
        if (state.trabajadorSeleccionadoBloqueado) {
            val fechaLegible = state.fecha.format(DateTimeFormatter.ofPattern("d 'de' MMM yyyy", Locale("es", "CL")))
            val nombreOperador = if (state.esServicioTransporte) {
                state.transportistaSeleccionado?.nombre
            } else {
                state.guiaSeleccionado?.nombre
            }
            _uiState.update {
                it.copy(errorMensaje = "$nombreOperador tiene bloqueado el $fechaLegible. Elige otro operador o cambia la fecha.")
            }
            return
        }

        val totalPax = state.adultos + state.ninos
        val infoExtra = buildList {
            state.clienteSeleccionado?.let { add("Cliente: ${it.nombre}") }
            state.idiomaSeleccionado?.let { add("Idioma: ${it.nombre}") }
            if (state.esServicioTransporte) {
                state.transportistaSeleccionado?.let { add("Driver: ${it.nombre}") }
                state.vehiculoSeleccionado?.let { add("Vehículo: ${it.tipo} (${it.patente})") }
            } else {
                state.guiaSeleccionado?.let { add("Guía: ${it.nombre}") }
            }
        }.joinToString(" · ")

        val nuevoServicio = Servicio(
            id = "SRV-LOC-${System.currentTimeMillis() % 100000}",
            numeroReserva = state.numeroReserva.trim().uppercase(),
            titulo = state.titulo.trim(),
            tipo = state.tipoServicio,
            fecha = state.fecha.toString(),
            horaInicio = state.horaInicio.trim(),
            horaFinEstimada = state.horaFinEstimada.trim(),
            lugarPickup = state.lugarPickup.trim(),
            lugarDropOff = state.lugarDropOff.trim(),
            cantidadPasajeros = totalPax,
            paxTexto = "${state.adultos}+${state.ninos}",
            driverId = if (state.esServicioTransporte) state.transportistaSeleccionado?.codigo else null,
            vehiculoId = if (state.esServicioTransporte) state.vehiculoSeleccionado?.let { "VEH-${it.id}" } else null,
            guiaId = if (!state.esServicioTransporte) state.guiaSeleccionado?.codigo else null,
            estado = EstadoServicio.ASIGNADO,
            tarifaAcordada = state.tarifaTexto.toDoubleOrNull() ?: 0.0,
            notasAgencia = state.notasAgencia.trim(),
            detalleItinerario = infoExtra
        )

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorMensaje = null) }
            repository.crearServicioLocal(state.sedeSeleccionada.codigo, nuevoServicio)
                .onSuccess {
                    _uiState.update { it.copy(guardando = false, servicioCreadoExito = true) }
                }
                .onFailure {
                    _uiState.update { it.copy(guardando = false, errorMensaje = "Error al guardar el servicio.") }
                }
        }
    }

    fun reiniciarFormulario(sedePorDefecto: Sede) {
        _uiState.update { state ->
            CrearServicioUiState(
                oficinas = state.oficinas,
                idiomas = state.idiomas,
                clientes = state.clientes,
                transportistas = state.transportistas,
                guias = state.guias,
                bloqueosPorTrabajador = state.bloqueosPorTrabajador,
                sedeSeleccionada = sedePorDefecto
            )
        }
        cargarStaffYDisponibilidad(sedePorDefecto, LocalDate.now())
    }
}
package com.pminteractive.pmtours.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.network.dto.GuiaDto
import com.pminteractive.pmtours.network.dto.ResumenDto
import com.pminteractive.pmtours.network.dto.TransportistaDto
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.repository.CoordinadorRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

typealias DiccionarioNombres = Map<String, String>

enum class SeccionCoordinador(val titulo: String) {
    SERVICIOS("Servicios"),
    TRANSPORTISTAS("Transportistas"),
    GUIAS("Guías")
}

data class ResumenCoordinador(
    val total: Int = 0,
    val pendientes: Int = 0,
    val confirmados: Int = 0,
    val rechazados: Int = 0
)

class CoordinadorViewModel(
    private val repository: CoordinadorRepository = AppContainer.coordinadorRepository
) : ViewModel() {

    private val _sedeSeleccionada = MutableStateFlow(Sede.SANTIAGO)
    val sedeSeleccionada: StateFlow<Sede> = _sedeSeleccionada.asStateFlow()

    private val _fechaSeleccionada = MutableStateFlow(LocalDate.now())
    val fechaSeleccionada: StateFlow<LocalDate> = _fechaSeleccionada.asStateFlow()

    private val _seccionActiva = MutableStateFlow(SeccionCoordinador.SERVICIOS)
    val seccionActiva: StateFlow<SeccionCoordinador> = _seccionActiva.asStateFlow()

    private val _queryBusqueda = MutableStateFlow("")
    val queryBusqueda: StateFlow<String> = _queryBusqueda.asStateFlow()

    private val _servicios = MutableStateFlow<List<Servicio>>(emptyList())
    val servicios: StateFlow<List<Servicio>> = _servicios.asStateFlow()

    private val _transportistas = MutableStateFlow<List<TransportistaDto>>(emptyList())
    val transportistas: StateFlow<List<TransportistaDto>> = _transportistas.asStateFlow()

    private val _guias = MutableStateFlow<List<GuiaDto>>(emptyList())
    val guias: StateFlow<List<GuiaDto>> = _guias.asStateFlow()

    private val _nombresTrabajadores = MutableStateFlow<DiccionarioNombres>(emptyMap())
    val nombresTrabajadores: StateFlow<DiccionarioNombres> = _nombresTrabajadores.asStateFlow()

    private val _servicioDetalle = MutableStateFlow<Servicio?>(null)
    val servicioDetalle: StateFlow<Servicio?> = _servicioDetalle.asStateFlow()

    private val _resumen = MutableStateFlow(ResumenCoordinador())
    val resumen: StateFlow<ResumenCoordinador> = _resumen.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _errorMensaje = MutableStateFlow<String?>(null)
    val errorMensaje: StateFlow<String?> = _errorMensaje.asStateFlow()

    private var sedeInicializada = false
    private var jobBusqueda: Job? = null
    private var ultimaBusquedaEjecutada: String = ""

    init {
        // Observamos en vivo la creación de servicios locales para actualizar la UI al instante
        viewModelScope.launch {
            repository.observarServiciosLocales().collect {
                if (_seccionActiva.value == SeccionCoordinador.SERVICIOS) {
                    cargarServicios()
                }
            }
        }
    }

    fun inicializar(sedeCoordinador: Sede) {
        if (!sedeInicializada) {
            _sedeSeleccionada.value = sedeCoordinador
            sedeInicializada = true
        }
        recargarTodo()
    }

    fun seleccionarSede(nuevaSede: Sede) {
        if (_sedeSeleccionada.value == nuevaSede) return
        _sedeSeleccionada.value = nuevaSede
        ultimaBusquedaEjecutada = _queryBusqueda.value.trim()
        recargarTodo()
    }

    fun seleccionarFecha(nuevaFecha: LocalDate) {
        if (_fechaSeleccionada.value == nuevaFecha) return
        _fechaSeleccionada.value = nuevaFecha
        cargarResumen()
        if (_seccionActiva.value == SeccionCoordinador.SERVICIOS) {
            cargarServicios()
        }
    }

    fun seleccionarSeccion(nuevaSeccion: SeccionCoordinador) {
        if (_seccionActiva.value == nuevaSeccion) return
        _seccionActiva.value = nuevaSeccion
        _queryBusqueda.value = ""
        ultimaBusquedaEjecutada = ""
        cargarSeccionActual()
    }

    fun actualizarBusqueda(texto: String) {
        _queryBusqueda.value = texto
        val textoLimpio = texto.trim()

        if (textoLimpio == ultimaBusquedaEjecutada) return

        jobBusqueda?.cancel()
        jobBusqueda = viewModelScope.launch {
            delay(400)
            ultimaBusquedaEjecutada = textoLimpio
            if (_seccionActiva.value != SeccionCoordinador.SERVICIOS) {
                cargarSeccionActual()
            }
        }
    }

    fun abrirDetalleServicio(servicio: Servicio) {
        _servicioDetalle.value = servicio
    }

    fun cerrarDetalleServicio() {
        _servicioDetalle.value = null
    }

    fun cancelarServicio(servicioId: String) {
        repository.registrarCambioServicio(
            servicioId = servicioId,
            nuevoEstado = EstadoServicio.CANCELADO
        )
        _servicioDetalle.update { actual ->
            if (actual?.id == servicioId) actual.copy(estado = EstadoServicio.CANCELADO) else actual
        }
        cargarServicios()
    }

    fun recargarTodo() {
        cargarDiccionarioNombres(_sedeSeleccionada.value.codigo)
        cargarResumen()
        cargarSeccionActual()
    }

    private fun cargarDiccionarioNombres(oficinaCod: String) {
        viewModelScope.launch {
            val drivers = repository.obtenerTransportistas(oficinaCod, null).getOrDefault(emptyList())
            val guiasLista = repository.obtenerGuias(oficinaCod, null).getOrDefault(emptyList())

            val mapaNuevo: DiccionarioNombres =
                drivers.associate { it.codigo to it.nombre } +
                        guiasLista.associate { it.codigo to it.nombre }

            _nombresTrabajadores.update { it + mapaNuevo }
        }
    }

    private fun cargarResumen() {
        val fechaStr = _fechaSeleccionada.value.toString()
        val oficinaCod = _sedeSeleccionada.value.codigo

        viewModelScope.launch {
            repository.obtenerResumen(fechaStr, oficinaCod).onSuccess { dto ->
                _resumen.value = mapearResumen(dto)
            }
        }
    }

    private fun cargarSeccionActual() {
        when (_seccionActiva.value) {
            SeccionCoordinador.SERVICIOS -> cargarServicios()
            SeccionCoordinador.TRANSPORTISTAS -> cargarTransportistas()
            SeccionCoordinador.GUIAS -> cargarGuias()
        }
    }

    private fun cargarServicios() {
        val fechaStr = _fechaSeleccionada.value.toString()
        val oficinaCod = _sedeSeleccionada.value.codigo

        viewModelScope.launch {
            _cargando.value = true
            _errorMensaje.value = null
            repository.obtenerServiciosPorDiaYSede(fechaStr, oficinaCod)
                .onSuccess { lista -> _servicios.value = lista }
                .onFailure { _errorMensaje.value = "No se pudieron cargar los servicios." }
            _cargando.value = false
        }
    }

    private fun cargarTransportistas() {
        val oficinaCod = _sedeSeleccionada.value.codigo
        val query = ultimaBusquedaEjecutada.takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            _cargando.value = true
            _errorMensaje.value = null
            repository.obtenerTransportistas(oficinaCod, query)
                .onSuccess { lista ->
                    _transportistas.value = lista
                    _nombresTrabajadores.update { actual ->
                        actual + lista.associate { it.codigo to it.nombre }
                    }
                }
                .onFailure { _errorMensaje.value = "No se pudieron cargar los transportistas." }
            _cargando.value = false
        }
    }

    private fun cargarGuias() {
        val oficinaCod = _sedeSeleccionada.value.codigo
        val query = ultimaBusquedaEjecutada.takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            _cargando.value = true
            _errorMensaje.value = null
            repository.obtenerGuias(oficinaCod, query)
                .onSuccess { lista ->
                    _guias.value = lista
                    _nombresTrabajadores.update { actual ->
                        actual + lista.associate { it.codigo to it.nombre }
                    }
                }
                .onFailure { _errorMensaje.value = "No se pudieron cargar los guías." }
            _cargando.value = false
        }
    }

    private fun mapearResumen(dto: ResumenDto): ResumenCoordinador {
        val conteo = dto.porStatusPL.associate { it.clave to it.servicios }
        return ResumenCoordinador(
            total = dto.totalServicios,
            confirmados = conteo["CF"] ?: 0,
            pendientes = (conteo["RQ"] ?: 0) + (conteo["SIN_ASIGNAR"] ?: 0),
            rechazados = (conteo["DL"] ?: 0) + (conteo["CX"] ?: 0)
        )
    }
}
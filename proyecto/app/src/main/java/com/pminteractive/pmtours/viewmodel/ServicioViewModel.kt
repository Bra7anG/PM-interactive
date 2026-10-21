/**
 * OBJETIVO DEL SCRIPT:
 * Gestionar la lógica de negocio, trazabilidad operativa y liquidación mensual de servicios para el operador.
 *
 * RESUMEN:
 * Expone la lista de viajes asignados de forma dinámica según el usuario autenticado,
 * filtra los servicios finalizados históricos para mantener limpia la agenda de trabajo,
 * procesa los hitos de Check-in y Check-out con validación horaria, y calcula el resumen financiero del mes.
 *
 * DETALLE:
 * - 'cargarServiciosDelUsuario()': Filtra la lista del repositorio para mostrar servicios activos (pendientes, confirmados, en ruta)
 *   y limita los servicios en estado FINALIZADO únicamente a los pertenecientes al mes actual ('YearMonth.now()').
 * - 'servicios': Expone la lista depurada mediante StateFlow para el consumo directo en 'ServiciosScreen'.
 * - 'resumenMesActual': Calcula automáticamente la liquidación mensual consolidando viajes finalizados y tarifas resguardadas por cancelaciones.
 * - 'realizarCheckIn()': Audita fecha y hora con 'LocalDateTime' evaluando la regla de puntualidad de 15 minutos de anticipación.
 */
package com.pminteractive.pmtours.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.RegistroHitoGps
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.repository.FakeServicioRepository
import com.pminteractive.pmtours.repository.ServicioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class ResumenLiquidacion(
    val totalAcumulado: Double = 0.0,
    val serviciosCompletados: Int = 0,
    val serviciosCanceladosConPago: Int = 0,
    val nombreMes: String = ""
)

class ServicioViewModel(
    private val repository: ServicioRepository = FakeServicioRepository(),
    trabajadorIdInicial: String = "TRA-001"
) : ViewModel() {

    private val _servicios = MutableStateFlow<List<Servicio>>(emptyList())
    val servicios: StateFlow<List<Servicio>> = _servicios.asStateFlow()

    private var jobObservador: Job? = null

    init {
        cargarServiciosDelUsuario(trabajadorIdInicial)
    }

    /**
     * Conecta la vista con el repositorio filtrando únicamente los viajes pertinentes a la agenda:
     * - Servicios activos/futuros: Siempre visibles (ASIGNADO, CONFIRMADO, EN_RUTA, CANCELADO).
     * - Servicios FINALIZADOS: Solo visibles si fueron ejecutados en el mes actual.
     */
    fun cargarServiciosDelUsuario(trabajadorId: String) {
        jobObservador?.cancel()
        jobObservador = viewModelScope.launch {
            repository.obtenerServiciosPorTrabajador(trabajadorId).collect { listaBruta ->
                val mesActual = YearMonth.now()

                val listaDepurada = listaBruta.filter { servicio ->
                    if (servicio.estado != EstadoServicio.FINALIZADO) {
                        // Viajes pendientes, confirmados, en ruta o con cancelación protegida se mantienen visibles
                        true
                    } else {
                        // Viajes finalizados solo se muestran si corresponden al mes en curso
                        try {
                            val fechaServicio = YearMonth.from(LocalDate.parse(servicio.fecha))
                            fechaServicio == mesActual
                        } catch (e: Exception) {
                            true
                        }
                    }
                }

                _servicios.value = listaDepurada
            }
        }
    }

    // Cálculo reactivo mensual para la pantalla de Perfil
    val resumenMesActual: StateFlow<ResumenLiquidacion> = servicios.map { lista ->
        val mesActual = YearMonth.now()
        val nombreMesFormateado = mesActual.format(
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "CL"))
        ).replaceFirstChar { it.uppercase() }

        var total = 0.0
        var completados = 0
        var canceladosPagados = 0

        lista.forEach { servicio ->
            try {
                val fechaServicio = YearMonth.from(LocalDate.parse(servicio.fecha))
                if (fechaServicio == mesActual) {
                    when (servicio.estado) {
                        EstadoServicio.FINALIZADO -> {
                            total += servicio.tarifaAcordada
                            completados++
                        }
                        EstadoServicio.CANCELADO -> {
                            // Política de resguardo: cancelaciones por no-show se abonan al operador
                            total += servicio.tarifaAcordada
                            canceladosPagados++
                        }
                        else -> Unit
                    }
                }
            } catch (e: Exception) {
                // Fechas con formato anómalo se descartan del cálculo
            }
        }

        ResumenLiquidacion(
            totalAcumulado = total,
            serviciosCompletados = completados,
            serviciosCanceladosConPago = canceladosPagados,
            nombreMes = nombreMesFormateado
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ResumenLiquidacion()
    )

    fun obtenerServicioPorId(id: String): Servicio? {
        return servicios.value.find { it.id == id }
    }

    fun responderAsignacion(servicioId: String, aceptar: Boolean) {
        viewModelScope.launch {
            repository.responderAsignacion(servicioId, aceptar)
        }
    }

    fun realizarCheckIn(servicioId: String) {
        val servicio = obtenerServicioPorId(servicioId) ?: return

        val aTiempo = try {
            val ahora = LocalDateTime.now()
            val fechaPactada = LocalDate.parse(servicio.fecha)
            val horaPactada = LocalTime.parse(servicio.horaInicio)
            val momentoPactado = LocalDateTime.of(fechaPactada, horaPactada)

            val minutosRestantes = ChronoUnit.MINUTES.between(ahora, momentoPactado)
            minutosRestantes >= 15
        } catch (e: Exception) {
            true
        }

        val hito = RegistroHitoGps(
            timestamp = System.currentTimeMillis(),
            latitud = -33.3934,
            longitud = -70.7956,
            direccionReferencia = servicio.lugarPickup,
            aTiempo = aTiempo
        )

        viewModelScope.launch {
            repository.registrarCheckIn(servicioId, hito)
        }
    }

    fun realizarCheckOut(servicioId: String) {
        val servicio = obtenerServicioPorId(servicioId) ?: return

        val hito = RegistroHitoGps(
            timestamp = System.currentTimeMillis(),
            latitud = -33.4372,
            longitud = -70.6506,
            direccionReferencia = servicio.lugarDropOff,
            aTiempo = true
        )

        viewModelScope.launch {
            repository.registrarCheckOut(servicioId, hito)
        }
    }
}
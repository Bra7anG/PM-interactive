package com.pminteractive.pmtours.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.repository.AuthRepository
import com.pminteractive.pmtours.repository.ServicioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AsignacionCoordinador(
    val servicioId: String,
    val nombreOperador: String,
    val hora: String,
    val tipo: String,
    val titulo: String,
    val estado: EstadoServicio
)

data class ResumenCoordinador(
    val pendientes: Int = 0,
    val confirmados: Int = 0,
    val rechazados: Int = 0
)

class CoordinadorViewModel(
    private val servicioRepository: ServicioRepository = AppContainer.servicioRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    private val _asignaciones = MutableStateFlow<List<AsignacionCoordinador>>(emptyList())
    val asignaciones: StateFlow<List<AsignacionCoordinador>> = _asignaciones.asStateFlow()

    private val _resumen = MutableStateFlow(ResumenCoordinador())
    val resumen: StateFlow<ResumenCoordinador> = _resumen.asStateFlow()

    private var jobObservador: Job? = null

    fun cargarServiciosDeSede(sede: Sede) {
        jobObservador?.cancel()
        jobObservador = viewModelScope.launch {
            val operadoresPorId = authRepository.obtenerUsuarios().associateBy { it.id }

            servicioRepository.obtenerTodosLosServicios().collect { servicios ->
                val serviciosDeSede = servicios.filter { servicio ->
                    val operadorId = servicio.driverId ?: servicio.guiaId
                    val operador = operadorId?.let { operadoresPorId[it] }
                    operador?.sede == sede
                }

                _asignaciones.value = serviciosDeSede.map { servicio ->
                    val operadorId = servicio.driverId ?: servicio.guiaId
                    val operador = operadorId?.let { operadoresPorId[it] }
                    AsignacionCoordinador(
                        servicioId = servicio.id,
                        nombreOperador = operador?.nombreCompleto ?: "Sin asignar",
                        hora = servicio.horaInicio,
                        tipo = servicio.tipo.descripcion,
                        titulo = servicio.titulo,
                        estado = servicio.estado
                    )
                }

                _resumen.value = ResumenCoordinador(
                    pendientes = serviciosDeSede.count { it.estado == EstadoServicio.ASIGNADO },
                    confirmados = serviciosDeSede.count { it.estado == EstadoServicio.CONFIRMADO },
                    rechazados = serviciosDeSede.count { it.estado == EstadoServicio.RECHAZADO }
                )
            }
        }
    }
}

package com.pminteractive.pmtours.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pminteractive.pmtours.model.servicio.RegistroHitoGps
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.repository.FakeServicioRepository
import com.pminteractive.pmtours.repository.ServicioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServicioViewModel(
    private val repository: ServicioRepository = FakeServicioRepository(),
    // Para probar la vista usamos el ID del chofer Carlos Morales ("DRV-001")
    trabajadorIdPrueba: String = "DRV-001"
) : ViewModel() {

    // Lista observable de viajes que Compose leerá en tiempo real
    val servicios: StateFlow<List<Servicio>> = repository
        .obtenerServiciosPorTrabajador(trabajadorIdPrueba)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun responderAsignacion(servicioId: String, aceptar: Boolean) {
        viewModelScope.launch {
            repository.responderAsignacion(servicioId, aceptar)
        }
    }

    fun realizarCheckIn(servicioId: String) {
        viewModelScope.launch {
            // Simulación de hito GPS marcando 15 min antes
            val hito = RegistroHitoGps(
                timestamp = System.currentTimeMillis(),
                latitud = -33.3934,
                longitud = -70.7956,
                direccionReferencia = "Punto de Pickup acordado",
                aTiempo = true
            )
            repository.registrarCheckIn(servicioId, hito)
        }
    }
}
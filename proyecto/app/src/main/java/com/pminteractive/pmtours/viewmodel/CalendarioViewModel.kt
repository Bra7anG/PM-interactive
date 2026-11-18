package com.pminteractive.pmtours.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.repository.DisponibilidadRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarioViewModel(
    private val repository: DisponibilidadRepository = AppContainer.disponibilidadRepository
) : ViewModel() {

    private val _diasBloqueados = MutableStateFlow<Set<LocalDate>>(emptySet())
    val diasBloqueados: StateFlow<Set<LocalDate>> = _diasBloqueados.asStateFlow()

    private var jobObservador: Job? = null
    private var trabajadorActual: String? = null

    fun cargarDiasBloqueados(trabajadorId: String) {
        if (trabajadorId.isBlank()) {
            limpiar()
            return
        }
        if (trabajadorId == trabajadorActual) return

        trabajadorActual = trabajadorId
        jobObservador?.cancel()
        _diasBloqueados.value = emptySet()

        jobObservador = viewModelScope.launch {
            repository.obtenerDiasBloqueados(trabajadorId).collect { dias ->
                _diasBloqueados.value = dias
            }
        }
    }

    fun alternarBloqueo(fecha: LocalDate, bloquear: Boolean) {
        val trabajadorId = trabajadorActual ?: return
        viewModelScope.launch {
            repository.alternarBloqueo(trabajadorId, fecha, bloquear)
        }
    }

    fun limpiar() {
        jobObservador?.cancel()
        jobObservador = null
        trabajadorActual = null
        _diasBloqueados.value = emptySet()
    }
}

package com.pminteractive.pmtours.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ResumenAdministrador(
    val totalUsuarios: Int = 0,
    val drivers: Int = 0,
    val guias: Int = 0,
    val coordinadores: Int = 0
)

class AdministradorViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    private val _usuarios = MutableStateFlow(authRepository.obtenerUsuarios())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    val resumen: StateFlow<ResumenAdministrador> = usuarios
        .map { lista -> calcularResumen(lista) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = calcularResumen(_usuarios.value)
        )

    private fun calcularResumen(lista: List<Usuario>) = ResumenAdministrador(
        totalUsuarios = lista.size,
        drivers = lista.count { it.rol == RolUsuario.DRIVER },
        guias = lista.count { it.rol == RolUsuario.GUIA },
        coordinadores = lista.count { it.rol == RolUsuario.COORDINADOR }
    )
}

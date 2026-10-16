package com.pminteractive.pmtours.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.repository.AuthRepository
import com.pminteractive.pmtours.repository.FakeAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Modela los estados posibles de la pantalla de inicio de sesión
sealed interface AuthUiState {
    data object Inactivo : AuthUiState
    data object Cargando : AuthUiState
    data class Autenticado(val usuario: Usuario) : AuthUiState
    data class Error(val mensaje: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository = FakeAuthRepository()
) : ViewModel() {

    // Estado reactivo que observará la pantalla LoginScreen
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Inactivo)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _uiState.value = AuthUiState.Error("Debe ingresar correo y contraseña.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando

            val resultado = authRepository.login(correo, contrasena)

            resultado.fold(
                onSuccess = { usuario ->
                    _uiState.value = AuthUiState.Autenticado(usuario)
                },
                onFailure = { excepcion ->
                    _uiState.value = AuthUiState.Error(excepcion.message ?: "Error al autenticar.")
                }
            )
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _uiState.value = AuthUiState.Inactivo
    }
}
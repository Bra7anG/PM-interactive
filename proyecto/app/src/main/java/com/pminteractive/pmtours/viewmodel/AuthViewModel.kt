/**
 * OBJETIVO DEL SCRIPT:
 * Gestionar la lógica de presentación y el estado visual del flujo de autenticación de usuarios.
 *
 * RESUMEN:
 * Controla el inicio y cierre de sesión en la aplicación, sirviendo de puente entre la pantalla
 * de Compose y el repositorio de autenticación mediante un estado reactivo observable.
 *
 * DETALLE:
 * - Define 'AuthUiState' como sealed interface para modelar los estados finitos de la vista:
 *   Inactivo, Cargando, Autenticado (con la instancia del Usuario) y Error (con mensaje explicativo).
 * - Aplica el patrón backing property exponiendo un 'StateFlow' inmutable (uiState) mientras
 *   mantiene un 'MutableStateFlow' privado (_uiState) para proteger las transiciones de estado.
 * - Ejecuta la verificación de credenciales de forma asíncrona en segundo plano mediante 'viewModelScope.launch',
 *   evitando congelar el hilo principal de la interfaz de usuario.
 * - Implementa validación previa de campos obligatorios (correo y contraseña no vacíos) antes de
 *   consultar a 'authRepository.login()'.
 * - Ofrece la función 'cerrarSesion()' para invalidar la sesión actual en el repositorio y
 *   restablecer la pantalla al estado Inactivo.
 */
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
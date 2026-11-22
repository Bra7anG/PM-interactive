package com.pminteractive.pmtours.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pminteractive.pmtours.R // <-- IMPORT AÑADIDO PARA LOS STRINGS
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed interface AuthUiState {
    data object Inactivo : AuthUiState // Muestra el formulario
    data object Cargando : AuthUiState // Muestra el Splash Screen
    data class Autenticado(val usuario: Usuario) : AuthUiState
    // CAMBIO: Ahora recibe el ID del recurso (Int) en vez de un String fijo
    data class Error(val mensajeResId: Int) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
    scope: CoroutineScope? = null
) : ViewModel() {

    private val coroutineScope: CoroutineScope = scope ?: viewModelScope

    // Obligamos a que la app nazca 100% en estado CARGANDO.
    // Compose dibujará el Splash Screen en el frame 0, sí o sí.
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Cargando)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Envolvemos la inicialización en una corrutina para forzar el delay artificial
        coroutineScope.launch {
            delay(1000) // 1 segundo (1000 ms) de espera para que se note el Splash
            verificarSesionGuardada()
        }
    }

    private fun verificarSesionGuardada() {
        val userGuardado = AppContainer.sharedPreferences.getString("saved_user", null)
        val passGuardada = AppContainer.sharedPreferences.getString("saved_pass", null)

        // Si hay credenciales, inicia el auto-login (el estado se mantiene en Cargando)
        if (!userGuardado.isNullOrBlank() && !passGuardada.isNullOrBlank()) {
            iniciarSesion(userGuardado, passGuardada)
        } else {
            // Solo si NO hay sesión guardada, cambiamos a Inactivo para revelar el formulario
            _uiState.value = AuthUiState.Inactivo
        }
    }

    fun iniciarSesion(usuario: String, contrasena: String) {
        if (usuario.isBlank() || contrasena.isBlank()) {
            _uiState.value = AuthUiState.Error(R.string.login_error_campos_vacios)
            return
        }

        coroutineScope.launch {
            _uiState.value = AuthUiState.Cargando

            val resultado = authRepository.login(usuario, contrasena)

            resultado.fold(
                onSuccess = { usuarioAutenticado ->
                    AppContainer.sharedPreferences.edit()
                        .putString("saved_user", usuario)
                        .putString("saved_pass", contrasena)
                        .apply()

                    _uiState.value = AuthUiState.Autenticado(usuarioAutenticado)
                },
                onFailure = {
                    // Reemplazamos el excepcion.message por un error genérico localizable
                    AppContainer.sharedPreferences.edit()
                        .clear()
                        .apply()
                    _uiState.value = AuthUiState.Error(R.string.login_error_autenticacion)
                }
            )
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        AppContainer.sharedPreferences.edit()
            .clear()
            .apply()
        _uiState.value = AuthUiState.Inactivo
    }
}
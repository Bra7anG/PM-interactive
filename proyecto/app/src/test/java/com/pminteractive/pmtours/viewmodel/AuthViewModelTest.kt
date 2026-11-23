package com.pminteractive.pmtours.viewmodel

import com.pminteractive.pmtours.support.RepositorioAuthFake
import com.pminteractive.pmtours.support.guiaPrueba
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthViewModelTest {

    private lateinit var scope: CoroutineScope

    @Before
    fun setUp() {
        scope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun estadoInicialEsCargando() {
        val viewModel = AuthViewModel(RepositorioAuthFake(), scope)
        assertEquals(AuthUiState.Cargando, viewModel.uiState.value)
    }

    @Test
    fun loginCorrecto() {
        val repositorio = RepositorioAuthFake(usuario = guiaPrueba(id = "GUI-012"))
        val viewModel = AuthViewModel(repositorio, scope)

        viewModel.iniciarSesion("valentina.lagos@pmtours.cl", "1234")

        val estado = viewModel.uiState.value
        assertTrue(estado is AuthUiState.Autenticado)
        assertEquals(
            "Valentina Lagos",
            (estado as AuthUiState.Autenticado).usuario.nombreCompleto
        )
        assertEquals("GUI-012", repositorio.obtenerUsuarioActual()?.id)
    }

    @Test
    fun claveIncorrecta() {
        val repositorio = RepositorioAuthFake(usuario = null)
        val viewModel = AuthViewModel(repositorio, scope)

        viewModel.iniciarSesion("valentina.lagos@pmtours.cl", "000")

        assertTrue(viewModel.uiState.value is AuthUiState.Error)
    }

    @Test
    fun correoDesconocido() {
        val repositorio = RepositorioAuthFake(usuario = null)
        val viewModel = AuthViewModel(repositorio, scope)

        viewModel.iniciarSesion("desconocido@pmtours.cl", "1234")

        assertTrue(viewModel.uiState.value is AuthUiState.Error)
    }

    @Test
    fun camposVacios() {
        val repositorio = RepositorioAuthFake()
        val viewModel = AuthViewModel(repositorio, scope)

        viewModel.iniciarSesion("", "")

        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        assertEquals(0, repositorio.llamadasLogin)
    }

    @Test
    fun estadoCargandoAntesDeResponder() {
        val puerta = CompletableDeferred<Unit>()
        val viewModel = AuthViewModel(
            RepositorioAuthFake(usuario = guiaPrueba(), puerta = puerta),
            scope
        )

        viewModel.iniciarSesion("valentina.lagos@pmtours.cl", "1234")

        assertEquals(AuthUiState.Cargando, viewModel.uiState.value)

        puerta.complete(Unit)

        assertTrue(viewModel.uiState.value is AuthUiState.Autenticado)
    }

    @Test
    fun cerrarSesionVuelveAInactivo() {
        val repositorio = RepositorioAuthFake(usuario = guiaPrueba())
        val viewModel = AuthViewModel(repositorio, scope)

        viewModel.iniciarSesion("valentina.lagos@pmtours.cl", "1234")
        assertTrue(viewModel.uiState.value is AuthUiState.Autenticado)

        viewModel.cerrarSesion()

        assertEquals(AuthUiState.Inactivo, viewModel.uiState.value)
        assertEquals(null, repositorio.obtenerUsuarioActual())
    }
}

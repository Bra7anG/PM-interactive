package com.pminteractive.pmtours.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.repository.FakeServicioRepository
import com.pminteractive.pmtours.ui.screens.driverGuia.ServiciosScreen
import com.pminteractive.pmtours.ui.theme.PMTOURSTheme
import com.pminteractive.pmtours.viewmodel.ServicioViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

class ServiciosScreenUITest {

    @get:Rule
    val rule = createComposeRule()

    private fun mostrarServicios(onServicioClick: (Servicio) -> Unit = {}) {
        val viewModel = ServicioViewModel(FakeServicioRepository())

        rule.setContent {
            PMTOURSTheme {
                ServiciosScreen(
                    nombreUsuario = "Carlos",
                    onServicioClick = onServicioClick,
                    viewModel = viewModel
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("servicio_card_SRV-000245")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun muestraSaludoYLista() {
        mostrarServicios()

        rule.onNodeWithText("Hola, Carlos").assertIsDisplayed()
        rule.onNodeWithTag("servicio_card_SRV-000245").assertIsDisplayed()
        rule.onNodeWithTag("servicio_card_SRV-000247").assertIsDisplayed()
    }

    @Test
    fun busquedaFiltraPorTexto() {
        mostrarServicios()

        rule.onNodeWithTag("servicios_busqueda").performTextInput("Cajón")

        rule.onNodeWithTag("servicio_card_SRV-000247").assertIsDisplayed()
        rule.onNodeWithTag("servicio_card_SRV-000245").assertDoesNotExist()
    }

    @Test
    fun chipPendientesFiltra() {
        mostrarServicios()

        rule.onNodeWithTag("chip_PENDIENTES").performClick()

        rule.onNodeWithTag("servicio_card_SRV-000245").assertIsDisplayed()
        rule.onNodeWithTag("servicio_card_SRV-000247").assertDoesNotExist()
    }

    @Test
    fun chipFinalizadosMuestraBanner() {
        mostrarServicios()

        rule.onNodeWithTag("chip_FINALIZADOS").performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodesWithText("Mostrando servicios finalizados de", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        rule.onNodeWithText("Mostrando servicios finalizados de", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun busquedaSinResultadosMuestraEstadoVacio() {
        mostrarServicios()

        rule.onNodeWithTag("servicios_busqueda").performTextInput("zzzzzz")

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("servicios_vacio").fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("servicios_vacio").assertIsDisplayed()
    }

    @Test
    fun clickEnTarjetaInvocaCallback() {
        val capturado = AtomicReference<Servicio?>()
        mostrarServicios(onServicioClick = { capturado.set(it) })

        rule.onNodeWithTag("servicio_card_SRV-000245").performClick()

        assertEquals("SRV-000245", capturado.get()?.id)
    }
}

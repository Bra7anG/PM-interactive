package com.pminteractive.pmtours.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pminteractive.pmtours.support.ContenidoTestEspanol
import com.pminteractive.pmtours.support.RepositorioServiciosFake
import com.pminteractive.pmtours.support.driverPrueba
import com.pminteractive.pmtours.ui.screens.driverGuia.CalendarioScreen
import com.pminteractive.pmtours.viewmodel.ServicioViewModel
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CalendarioScreenUITest {

    @get:Rule
    val rule = createComposeRule()

    private fun mostrarCalendario() {
        val usuario = driverPrueba()
        val viewModel = ServicioViewModel(
            repository = RepositorioServiciosFake(emptyList()),
            trabajadorIdInicial = usuario.id
        )

        rule.setContent {
            ContenidoTestEspanol {
                CalendarioScreen(
                    usuario = usuario,
                    servicioViewModel = viewModel
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("calendario_prompt").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun muestraTituloLeyendaYPrompt() {
        mostrarCalendario()

        rule.onNodeWithText("Mi disponibilidad").assertIsDisplayed()
        rule.onNodeWithText("Bloqueado").assertIsDisplayed()
        rule.onNodeWithText("Con servicio").assertIsDisplayed()
        rule.onNodeWithTag("calendario_prompt").assertIsDisplayed()
    }

    @Test
    fun seleccionarFechaSinAnticipacionMuestraAdvertencia() {
        mostrarCalendario()

        rule.onNodeWithTag("dia_${LocalDate.now()}").performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("calendario_aviso").fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("Los días libres deben solicitarse con al menos 1 semana de anticipación.")
            .assertIsDisplayed()
        rule.onNodeWithTag("switch_bloqueo").assertIsNotEnabled()
    }
}

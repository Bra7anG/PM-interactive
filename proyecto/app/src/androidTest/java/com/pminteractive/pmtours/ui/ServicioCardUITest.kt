package com.pminteractive.pmtours.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.model.servicio.TipoServicio
import com.pminteractive.pmtours.support.servicioPrueba
import com.pminteractive.pmtours.ui.components.ServicioCard
import com.pminteractive.pmtours.ui.theme.PMTOURSTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class ServicioCardUITest {

    @get:Rule
    val rule = createComposeRule()

    private fun mostrarCard(servicio: Servicio, onClick: () -> Unit = {}) {
        rule.setContent {
            PMTOURSTheme {
                ServicioCard(
                    servicio = servicio,
                    onCardClick = onClick
                )
            }
        }
    }

    @Test
    fun muestraHoraTipoYEstadoPendiente() {
        mostrarCard(
            servicioPrueba(
                id = "CARD",
                estado = EstadoServicio.ASIGNADO,
                horaInicio = "09:30",
                tipo = TipoServicio.TRANSFER
            )
        )

        rule.onNodeWithText("09:30").assertIsDisplayed()
        rule.onNodeWithText("TRANSFER").assertIsDisplayed()
        rule.onNodeWithTag("servicio_estado_CARD", useUnmergedTree = true)
            .assertTextEquals("Pendiente")
    }

    @Test
    fun muestraEstadoConfirmado() {
        mostrarCard(
            servicioPrueba(
                id = "CONFIRMADO",
                estado = EstadoServicio.CONFIRMADO
            )
        )

        rule.onNodeWithTag("servicio_estado_CONFIRMADO", useUnmergedTree = true)
            .assertTextEquals("Confirmado")
    }

    @Test
    fun muestraEstadoEnRuta() {
        mostrarCard(
            servicioPrueba(
                id = "EN_RUTA",
                estado = EstadoServicio.EN_RUTA
            )
        )

        rule.onNodeWithTag("servicio_estado_EN_RUTA", useUnmergedTree = true)
            .assertTextEquals("En ruta")
    }

    @Test
    fun clickInvocaCallback() {
        val invocado = AtomicBoolean(false)
        mostrarCard(
            servicio = servicioPrueba(id = "CLICK"),
            onClick = { invocado.set(true) }
        )

        rule.onNodeWithTag("servicio_card_CLICK").performClick()

        assertTrue(invocado.get())
    }
}

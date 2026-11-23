package com.pminteractive.pmtours.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.support.ContenidoTestEspanol
import com.pminteractive.pmtours.support.RepositorioServiciosFake
import com.pminteractive.pmtours.support.servicioPrueba
import com.pminteractive.pmtours.ui.screens.driverGuia.DetalleServicioScreen
import com.pminteractive.pmtours.viewmodel.ServicioViewModel
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class DetalleServicioScreenUITest {

    @get:Rule
    val rule = createComposeRule()

    private fun mostrarServicio(servicio: Servicio, servicioId: String = servicio.id) {
        val repositorio = RepositorioServiciosFake(listOf(servicio))
        val viewModel = ServicioViewModel(
            repository = repositorio,
            trabajadorIdInicial = servicio.driverId ?: servicio.guiaId ?: "TRA-001"
        )

        rule.setContent {
            ContenidoTestEspanol {
                DetalleServicioScreen(
                    servicioId = servicioId,
                    usuarioId = servicio.driverId ?: servicio.guiaId ?: "TRA-001",
                    onVolver = {},
                    viewModel = viewModel
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("detalle_titulo", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty() ||
                    rule.onAllNodesWithTag("detalle_no_encontrado", useUnmergedTree = true)
                        .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun servicioAsignadoMuestraBotonConfirmar() {
        mostrarServicio(
            servicioPrueba(
                id = "ASIGNADO",
                estado = EstadoServicio.ASIGNADO,
                fecha = LocalDate.now().toString()
            )
        )

        rule.onNodeWithTag("btn_confirmar").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun flujoCompletoConfirmarCheckInYCheckOut() {
        mostrarServicio(
            servicioPrueba(
                id = "FLUJO",
                estado = EstadoServicio.ASIGNADO,
                fecha = LocalDate.now().toString()
            )
        )

        rule.onNodeWithTag("btn_confirmar").performScrollTo().performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("btn_checkin").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("btn_checkin").performScrollTo().performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("btn_checkout").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("btn_checkout").performScrollTo().performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodesWithText("Servicio completado exitosamente")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        rule.onNodeWithText("Servicio completado exitosamente")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun servicioConfirmadoFuturoMuestraCheckInProgramado() {
        mostrarServicio(
            servicioPrueba(
                id = "FUTURO",
                estado = EstadoServicio.CONFIRMADO,
                fecha = LocalDate.now().plusDays(1).toString()
            )
        )

        rule.onNodeWithText("Check-in programado").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("btn_checkin").assertDoesNotExist()
    }

    @Test
    fun servicioFinalizadoMuestraMensaje() {
        mostrarServicio(
            servicioPrueba(
                id = "FINALIZADO",
                estado = EstadoServicio.FINALIZADO,
                fecha = LocalDate.now().toString()
            )
        )

        rule.onNodeWithText("Servicio completado exitosamente")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun servicioCanceladoMuestraMensaje() {
        mostrarServicio(
            servicioPrueba(
                id = "CANCELADO",
                estado = EstadoServicio.CANCELADO,
                fecha = LocalDate.now().toString()
            )
        )

        rule.onNodeWithText("Servicio cancelado por coordinación o cliente (Tarifa resguardada)")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun servicioInexistenteMuestraNoEncontrado() {
        mostrarServicio(
            servicioPrueba(id = "EXISTE"),
            servicioId = "NO-EXISTE"
        )

        rule.onNodeWithTag("detalle_no_encontrado").assertIsDisplayed()
    }
}

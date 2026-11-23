package com.pminteractive.pmtours.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.model.servicio.EstadoServicio
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.support.ContenidoTestEspanol
import com.pminteractive.pmtours.support.RepositorioServiciosFake
import com.pminteractive.pmtours.support.driverPrueba
import com.pminteractive.pmtours.support.guiaPrueba
import com.pminteractive.pmtours.support.servicioPrueba
import com.pminteractive.pmtours.ui.screens.driverGuia.PerfilScreen
import com.pminteractive.pmtours.viewmodel.ServicioViewModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class PerfilScreenUITest {

    @get:Rule
    val rule = createComposeRule()

    private fun mostrarPerfil(
        usuario: Usuario,
        servicios: List<Servicio> = emptyList(),
        onCerrarSesion: () -> Unit = {}
    ) {
        val repositorio = RepositorioServiciosFake(servicios)
        val viewModel = ServicioViewModel(repositorio, usuario.id)

        rule.setContent {
            ContenidoTestEspanol {
                PerfilScreen(
                    usuario = usuario,
                    onCerrarSesion = onCerrarSesion,
                    servicioViewModel = viewModel
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("perfil_nombre").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun muestraDatosDelUsuarioYBanco() {
        mostrarPerfil(driverPrueba())

        rule.onNodeWithTag("perfil_nombre").assertTextEquals("Carlos Morales")
        rule.onNodeWithTag("perfil_rut").assertTextEquals("RUT: 18.452.123-K")
        rule.onNodeWithText("BancoEstado · Cuenta RUT").assertIsDisplayed()
    }

    @Test
    fun muestraLiquidacionDelMes() {
        val hoy = LocalDate.now().toString()
        mostrarPerfil(
            usuario = driverPrueba(),
            servicios = listOf(
                servicioPrueba(
                    id = "FINALIZADO",
                    estado = EstadoServicio.FINALIZADO,
                    fecha = hoy,
                    tarifa = 45_000.0
                ),
                servicioPrueba(
                    id = "CANCELADO",
                    estado = EstadoServicio.CANCELADO,
                    fecha = hoy,
                    tarifa = 30_000.0
                )
            )
        )

        val esperado = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            maximumFractionDigits = 0
        }.format(75_000.0)

        rule.waitUntil(5_000) {
            rule.onAllNodesWithText(esperado).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("perfil_total").assertTextEquals(esperado)
        rule.onNodeWithTag("perfil_finalizados").assertTextEquals("1")
        rule.onNodeWithTag("perfil_tarifa_protegida").assertTextEquals("1")
    }

    @Test
    fun muestraFlotaSiEsDriver() {
        mostrarPerfil(driverPrueba())

        rule.onNodeWithText("Flota de Vehículos Habilitada")
            .performScrollTo()
            .assertIsDisplayed()
        rule.onNodeWithText("Toyota RAV4 2021 (SEDAN)")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun muestraIdiomasSiEsGuia() {
        mostrarPerfil(guiaPrueba())

        rule.onNodeWithText("Competencias e Idiomas")
            .performScrollTo()
            .assertIsDisplayed()
        rule.onNodeWithText("Idiomas: Español, Inglés")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun clickCerrarSesionInvocaCallback() {
        val invocado = AtomicBoolean(false)
        mostrarPerfil(driverPrueba(), onCerrarSesion = { invocado.set(true) })

        rule.onNodeWithTag("btn_cerrar_sesion").performScrollTo().performClick()

        assertTrue(invocado.get())
    }
}

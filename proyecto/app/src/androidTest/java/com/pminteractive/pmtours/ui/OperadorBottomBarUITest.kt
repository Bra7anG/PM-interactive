package com.pminteractive.pmtours.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pminteractive.pmtours.ui.components.OperadorBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.PMTOURSTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OperadorBottomBarUITest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun muestraTabsYServiciosSeleccionado() {
        rule.setContent {
            PMTOURSTheme {
                OperadorBottomBar(
                    rutaActual = Screen.Servicios.route,
                    onNavegar = {}
                )
            }
        }

        rule.onNodeWithText("Servicios").assertIsDisplayed()
        rule.onNodeWithText("Calendario").assertIsDisplayed()
        rule.onNodeWithText("Perfil").assertIsDisplayed()
        rule.onNodeWithTag("tab_servicios").assertIsSelected()
    }

    @Test
    fun clickTabsInvocaNavegacion() {
        var destino: String? = null

        rule.setContent {
            PMTOURSTheme {
                OperadorBottomBar(
                    rutaActual = Screen.Servicios.route,
                    onNavegar = { destino = it }
                )
            }
        }

        rule.onNodeWithTag("tab_calendario").performClick()
        assertEquals(Screen.Calendario.route, destino)

        rule.onNodeWithTag("tab_perfil").performClick()
        assertEquals(Screen.Perfil.route, destino)
    }
}

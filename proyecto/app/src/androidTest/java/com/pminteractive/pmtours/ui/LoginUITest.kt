package com.pminteractive.pmtours.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.pminteractive.pmtours.MainActivity
import org.junit.Rule
import org.junit.Test

class LoginUITest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun botonVisible() {
        rule.onNodeWithText("Ingresar").assertIsDisplayed()
    }

    @Test
    fun loginCorrecto() {
        rule.onNodeWithTag("login_correo").performTextInput("valentina.lagos@pmtours.cl")
        rule.onNodeWithTag("login_clave").performTextInput("1234")
        rule.onNodeWithTag("login_btn").performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodesWithText("Hola, Valentina").fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("Hola, Valentina").assertIsDisplayed()
    }

    @Test
    fun loginIncorrecto() {
        rule.onNodeWithTag("login_correo").performTextInput("desconocido@pmtours.cl")
        rule.onNodeWithTag("login_clave").performTextInput("1234")
        rule.onNodeWithTag("login_btn").performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodesWithText("Credenciales incorrectas o usuario no encontrado.")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        rule.onNodeWithText("Credenciales incorrectas o usuario no encontrado.")
            .assertIsDisplayed()
    }

    @Test
    fun camposVacios() {
        rule.onNodeWithTag("login_btn").performClick()

        rule.onNodeWithText("Ingrese su usuario o correo").assertIsDisplayed()
        rule.onNodeWithText("Contraseña incorrecta").assertIsDisplayed()
    }
}

package com.pminteractive.pmtours.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import android.content.Context
import com.pminteractive.pmtours.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginUITest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun limpiarSesion() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("lince_session", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun esperarFormularioLogin() {
        rule.waitUntil(10_000) {
            rule.onAllNodesWithTag("login_btn")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun botonVisible() {
        esperarFormularioLogin()
        rule.onNodeWithText("Ingresar").assertIsDisplayed()
    }

    @Test
    fun loginCorrecto() {
        esperarFormularioLogin()

        rule.onNodeWithTag("login_correo").performTextInput("andres.godoy@pmtours.cl")
        rule.onNodeWithTag("login_clave").performTextInput("1234")
        rule.onNodeWithTag("login_btn").performClick()

        rule.waitUntil(10_000) {
            rule.onAllNodesWithText("Andrés", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun loginIncorrecto() {
        esperarFormularioLogin()

        rule.onNodeWithTag("login_correo").performTextInput("desconocido@pmtours.cl")
        rule.onNodeWithTag("login_clave").performTextInput("1234")
        rule.onNodeWithTag("login_btn").performClick()

        rule.waitUntil(10_000) {
            rule.onAllNodesWithText("Error al autenticar", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun camposVacios() {
        esperarFormularioLogin()

        rule.onNodeWithTag("login_btn").performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodesWithText("Ingrese un usuario", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        rule.onNodeWithText("Contraseña incorrecta").assertIsDisplayed()
    }
}

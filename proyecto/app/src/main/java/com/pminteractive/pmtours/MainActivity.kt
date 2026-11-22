package com.pminteractive.pmtours

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.ui.navigation.AppNavigation
import com.pminteractive.pmtours.ui.theme.PMTOURSTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    // 1. Inyectamos el idioma guardado antes de que la app construya la UI
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("lince_session", Context.MODE_PRIVATE)
        val idiomaGuardado = prefs.getString("idioma_app", "es") ?: "es"

        val locale = Locale(idiomaGuardado)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val contextModificado = newBase.createConfigurationContext(config)
        super.attachBaseContext(contextModificado)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.inicializar(this)

        // 2. Refuerzo de seguridad para Jetpack Compose
        val idiomaGuardado = AppContainer.sharedPreferences.getString("idioma_app", "es") ?: "es"
        val locale = Locale(idiomaGuardado)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        setContent {
            PMTOURSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
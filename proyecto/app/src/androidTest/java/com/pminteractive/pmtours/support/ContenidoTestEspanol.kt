package com.pminteractive.pmtours.support

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.pminteractive.pmtours.ui.theme.PMTOURSTheme
import java.util.Locale

fun Context.forzarEspanol(): Context {
    val config = Configuration(resources.configuration)
    config.setLocale(Locale("es", "CL"))
    return createConfigurationContext(config)
}

@Composable
fun ContenidoTestEspanol(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val contextEs = remember(context) { context.forzarEspanol() }
    CompositionLocalProvider(LocalContext provides contextEs) {
        PMTOURSTheme {
            content()
        }
    }
}

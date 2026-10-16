package com.pminteractive.pmtours.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de colores basado en la identidad de Proyecto Lince
private val LinceColorScheme = darkColorScheme(
    primary = LinceCyan,
    onPrimary = Color.White,
    secondary = LinceCyanHover,
    onSecondary = Color.White,
    background = LinceBackground,
    onBackground = LinceTextPrimary,
    surface = LinceSurface,
    onSurface = LinceTextPrimary,
    surfaceVariant = LinceSurface,
    onSurfaceVariant = LinceTextSecondary,
    outline = LinceSurfaceBorder,
    error = LinceError,
    onError = Color.White
)

@Composable
fun PMTOURSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Se desactiva el color dinámico para conservar los colores corporativos
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> LinceColorScheme
    }

    // Pinta la barra de estado superior del celular con el azul marino de fondo
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LinceBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
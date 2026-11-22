package com.pminteractive.pmtours.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.ui.theme.LinceCyan
import com.pminteractive.pmtours.ui.theme.LinceTextSecondary
import com.pminteractive.pmtours.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorIdioma(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = AppContainer.sharedPreferences

    // Por defecto es "es" (Español)
    var idiomaActual by remember { mutableStateOf(prefs.getString("idioma_app", "es") ?: "es") }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.idioma_titulo),
                color = LinceCyan,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 14.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = idiomaActual == "es",
                    onClick = {
                        if (idiomaActual != "es") {
                            prefs.edit().putString("idioma_app", "es").apply()
                            idiomaActual = "es"
                            (context as? Activity)?.recreate() // Reinicia la pantalla
                        }
                    },
                    label = { Text("Español", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LinceCyan.copy(alpha = 0.2f),
                        selectedLabelColor = LinceCyan,
                        labelColor = LinceTextSecondary
                    ),
                    border = null
                )

                FilterChip(
                    selected = idiomaActual == "en",
                    onClick = {
                        if (idiomaActual != "en") {
                            prefs.edit().putString("idioma_app", "en").apply()
                            idiomaActual = "en"
                            (context as? Activity)?.recreate() // Reinicia la pantalla
                        }
                    },
                    label = { Text("English", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LinceCyan.copy(alpha = 0.2f),
                        selectedLabelColor = LinceCyan,
                        labelColor = LinceTextSecondary
                    ),
                    border = null
                )
            }
        }
    }
}
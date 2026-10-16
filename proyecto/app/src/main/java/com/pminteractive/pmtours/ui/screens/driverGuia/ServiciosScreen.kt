package com.pminteractive.pmtours.ui.screens.operador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.model.servicio.Servicio
import com.pminteractive.pmtours.ui.components.OperadorBottomBar
import com.pminteractive.pmtours.ui.components.ServicioCard
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.ServicioViewModel

@Composable
fun ServiciosScreen(
    nombreUsuario: String = "Carlos",
    onServicioClick: (Servicio) -> Unit = {},
    onCambiarTab: (String) -> Unit = {},
    viewModel: ServicioViewModel = viewModel()
) {
    val servicios by viewModel.servicios.collectAsState()
    var textoBusqueda by remember { mutableStateOf("") }

    val fechaCabecera = remember {
        try {
            val hoy = java.time.LocalDate.now()
            val formato = java.time.format.DateTimeFormatter.ofPattern(
                "EEEE d 'de' MMMM",
                java.util.Locale("es", "CL")
            )
            hoy.format(formato).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "Hoy"
        }
    }

    val serviciosFiltrados = remember(servicios, textoBusqueda) {
        if (textoBusqueda.isBlank()) servicios
        else {
            val query = textoBusqueda.trim().lowercase()
            servicios.filter {
                it.titulo.lowercase().contains(query) ||
                        it.fecha.contains(query) ||
                        it.estado.name.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            OperadorBottomBar(
                rutaActual = Screen.Servicios.route,
                onNavegar = onCambiarTab
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = fechaCabecera, color = LinceTextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Hola, $nombreUsuario", color = LinceTextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar por fecha o estado", color = LinceTextSecondary.copy(alpha = 0.7f), fontSize = 14.sp) },
                leadingIcon = { Text("🔍", fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LinceSurface,
                    unfocusedContainerColor = LinceSurface,
                    focusedTextColor = LinceTextPrimary,
                    unfocusedTextColor = LinceTextPrimary,
                    cursorColor = LinceCyan,
                    focusedBorderColor = LinceCyan,
                    unfocusedBorderColor = LinceSurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(serviciosFiltrados, key = { it.id }) { servicio ->
                    ServicioCard(
                        servicio = servicio,
                        onCardClick = { onServicioClick(servicio) }
                    )
                }
            }
        }
    }
}
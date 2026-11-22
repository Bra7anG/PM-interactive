package com.pminteractive.pmtours.ui.screens.administrador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset // IMPORT CORREGIDO
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.ui.components.AdminBottomBar
import com.pminteractive.pmtours.ui.screens.coordinador.CrearServicioScreen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.AdministradorViewModel
import kotlinx.coroutines.launch

@Composable
fun CrearAdminScreen(
    usuario: Usuario?,
    onCambiarTab: (String) -> Unit,
    onServicioCreado: () -> Unit,
    viewModel: AdministradorViewModel = viewModel()
) {
    var tabSeleccionada by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            AdminBottomBar(
                rutaActual = com.pminteractive.pmtours.ui.navigation.Screen.CrearAdministrador.route,
                onNavegar = onCambiarTab
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TabRow Superior
            TabRow(
                selectedTabIndex = tabSeleccionada,
                containerColor = LinceSurface,
                contentColor = LinceCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabSeleccionada]),
                        color = LinceCyan
                    )
                }
            ) {
                Tab(
                    selected = tabSeleccionada == 0,
                    onClick = { tabSeleccionada = 0 },
                    text = { Text("Crear Servicio", fontWeight = FontWeight.Bold) },
                    selectedContentColor = LinceCyan,
                    unselectedContentColor = LinceTextSecondary
                )
                Tab(
                    selected = tabSeleccionada == 1,
                    onClick = { tabSeleccionada = 1 },
                    text = { Text("Crear Usuario", fontWeight = FontWeight.Bold) },
                    selectedContentColor = LinceCyan,
                    unselectedContentColor = LinceTextSecondary
                )
            }

            // Contenido dinámico según la pestaña
            if (tabSeleccionada == 0) {
                // Incrustamos tu pantalla existente pero Ocultándole su propio Scaffold
                Box(modifier = Modifier.fillMaxSize()) {
                    CrearServicioScreen(
                        usuario = usuario,
                        onCambiarTab = onCambiarTab,
                        onServicioCreado = onServicioCreado,
                        esModoAdminIncrustado = true
                    )
                }
            } else {
                // Formulario de Usuario Local
                FormularioUsuarioLocalAdmin(
                    onUsuarioCreado = {
                        viewModel.cargarTodosLosUsuarios()
                        onCambiarTab(com.pminteractive.pmtours.ui.navigation.Screen.PanelAdministrador.route)
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioUsuarioLocalAdmin(
    onUsuarioCreado: () -> Unit,
    viewModel: AdministradorViewModel
) {
    var nombre by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.DRIVER) }
    var sedeSeleccionada by remember { mutableStateOf(Sede.SANTIAGO) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nueva Cuenta Local",
                color = LinceTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Los usuarios creados aquí solo existirán en la memoria de este dispositivo (sin sincronización con el servidor Lince).",
                color = LinceTextSecondary,
                fontSize = 13.sp
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LinceCyan,
                    focusedLabelColor = LinceCyan
                )
            )

            Text("Rol Operativo", color = LinceTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(RolUsuario.DRIVER, RolUsuario.GUIA, RolUsuario.COORDINADOR).forEach { rol ->
                    FilterChip(
                        selected = rolSeleccionado == rol,
                        onClick = { rolSeleccionado = rol },
                        label = { Text(rol.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LinceCyan.copy(alpha = 0.2f),
                            selectedLabelColor = LinceCyan
                        )
                    )
                }
            }

            Text("Sede Base", color = LinceTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sede.values().filter { it.codigo != "ALL" }.forEach { sede ->
                    FilterChip(
                        selected = sedeSeleccionada == sede,
                        onClick = { sedeSeleccionada = sede },
                        label = { Text(sede.nombreCiudad, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LinceCyan.copy(alpha = 0.2f),
                            selectedLabelColor = LinceCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        coroutineScope.launch {
                            val resultado = viewModel.crearUsuarioManual(nombre, rolSeleccionado, sedeSeleccionada)
                            if (resultado.isSuccess) {
                                onUsuarioCreado()
                            } else {
                                snackbarHostState.showSnackbar(resultado.exceptionOrNull()?.message ?: "Error al crear usuario")
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("El nombre es obligatorio")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LinceCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Registrar Usuario Local", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
package com.pminteractive.pmtours.ui.screens.administrador

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // <-- IMPORT AÑADIDO
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.datosoperativos.Sede
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.ui.components.AdminBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.AdministradorViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Data class auxiliar para armar el selector de roles con contadores
private data class OpcionFiltroRol(val etiqueta: String, val rol: RolUsuario?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelAdministradorScreen(
    usuario: Usuario?,
    onCambiarTab: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    viewModel: AdministradorViewModel = viewModel()
) {
    val usuariosFiltrados by viewModel.usuariosFiltrados.collectAsState()
    val resumen by viewModel.resumen.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val filtroRol by viewModel.filtroRol.collectAsState()
    val filtroSede by viewModel.filtroSede.collectAsState()

    val context = LocalContext.current

    val fallbackFecha = stringResource(id = R.string.admin_panel_fecha_fallback)
    val fechaCabecera = remember {
        try {
            val hoy = LocalDate.now()
            // Locale.getDefault() para adaptar "Monday" / "Lunes"
            val formato = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.getDefault())
            hoy.format(formato).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            fallbackFecha
        }
    }

    // Traducimos las etiquetas de los filtros
    val lblTodos = stringResource(id = R.string.admin_filtro_roles_todos)
    val lblDrivers = stringResource(id = R.string.admin_filtro_roles_drivers, resumen.drivers)
    val lblGuias = stringResource(id = R.string.admin_filtro_roles_guias, resumen.guias)
    val lblStaff = stringResource(id = R.string.admin_filtro_roles_staff, resumen.coordinadores)

    val opcionesRol = remember(resumen, lblTodos, lblDrivers, lblGuias, lblStaff) {
        listOf(
            OpcionFiltroRol(lblTodos, null),
            OpcionFiltroRol(lblDrivers, RolUsuario.DRIVER),
            OpcionFiltroRol(lblGuias, RolUsuario.GUIA),
            OpcionFiltroRol(lblStaff, RolUsuario.COORDINADOR)
        )
    }

    val opcionRolActual = opcionesRol.find { it.rol == filtroRol } ?: opcionesRol.first()

    val opcionesSede = remember {
        listOf(null) + Sede.values().filter { it.codigo != "ALL" }
    }

    val lblTodasSedes = stringResource(id = R.string.admin_filtro_sedes_todas)
    val etiquetaSedeActual = filtroSede?.nombreCiudad ?: lblTodasSedes

    val textoCompartir = stringResource(id = R.string.admin_panel_compartir)

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            AdminBottomBar(
                rutaActual = Screen.PanelAdministrador.route,
                onNavegar = onCambiarTab
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. CABECERA LIMPIA Y BOTÓN REPORTE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = fechaCabecera,
                        color = LinceTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.admin_panel_titulo),
                        color = LinceTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        val uri = viewModel.generarArchivoReporte(context)
                        if (uri != null) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, textoCompartir))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LinceCyan),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(painterResource(id = R.drawable.ic_info), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(id = R.string.admin_panel_btn_reporte), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LinceBackground)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. SISTEMA DE FILTROS (Desplegables estilo Coordinador)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SelectorCompactoDropdown(
                    etiquetaActual = opcionRolActual.etiqueta,
                    opciones = opcionesRol,
                    obtenerTexto = { it.etiqueta },
                    onSeleccionar = { viewModel.setFiltroRol(it.rol) },
                    destacar = filtroRol != null, // Se ilumina si hay un rol específico seleccionado
                    modifier = Modifier.weight(1f)
                )

                SelectorCompactoDropdown(
                    etiquetaActual = etiquetaSedeActual,
                    opciones = opcionesSede,
                    obtenerTexto = { it?.nombreCiudad ?: lblTodasSedes },
                    onSeleccionar = { viewModel.setFiltroSede(it) },
                    destacar = filtroSede != null, // Se ilumina si hay una sede específica seleccionada
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = LinceSurfaceBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // 3. LISTA DE USUARIOS CON INDICADOR DE CARGA
            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LinceCyan)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(usuariosFiltrados, key = { it.id }) { cuenta ->
                        FilaCuenta(cuenta)
                    }
                }
            }
        }
    }
}

@Composable
private fun < T > SelectorCompactoDropdown(
    etiquetaActual: String,
    opciones: List< T >,
    obtenerTexto: (T) -> String,
    onSeleccionar: (T) -> Unit,
    destacar: Boolean,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (destacar) LinceCyan.copy(alpha = 0.14f) else LinceSurface)
                .border(
                    width = 1.dp,
                    color = if (destacar) LinceCyan else LinceSurfaceBorder,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { expandido = true }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = etiquetaActual,
                color = if (destacar) LinceCyan else LinceTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "▾",
                color = if (destacar) LinceCyan else LinceTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
            modifier = Modifier
                .background(LinceSurface)
                .border(1.dp, LinceSurfaceBorder)
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = obtenerTexto(opcion),
                            color = LinceTextPrimary,
                            fontSize = 13.sp
                        )
                    },
                    onClick = {
                        onSeleccionar(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FilaCuenta(cuenta: Usuario) {
    val (fondoBadge, colorBadge, etiquetaBadge) = estiloRol(cuenta.rol)

    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cuenta.nombreCompleto,
                    color = LinceTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = cuenta.correo,
                    color = LinceTextSecondary,
                    fontSize = 11.sp
                )
            }

            Surface(
                color = fondoBadge,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = etiquetaBadge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    color = colorBadge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Convertido a @Composable para permitir el uso de stringResource
@Composable
private fun estiloRol(rol: RolUsuario): Triple<Color, Color, String> = when (rol) {
    RolUsuario.ADMINISTRADOR -> Triple(LinceRoleAdminSoft, LinceRoleAdmin, stringResource(id = R.string.admin_badge_admin))
    RolUsuario.COORDINADOR -> Triple(LinceRoleCoordinatorSoft, LinceRoleCoordinator, stringResource(id = R.string.admin_badge_coord))
    RolUsuario.DRIVER -> Triple(LinceRoleDriverSoft, LinceRoleDriver, stringResource(id = R.string.admin_badge_driver))
    RolUsuario.GUIA -> Triple(LinceRoleGuideSoft, LinceRoleGuide, stringResource(id = R.string.admin_badge_guia))
}
package com.pminteractive.pmtours.ui.screens.administrador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.datosoperativos.RolUsuario
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.ui.components.PanelBottomBar
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.AdministradorViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PanelAdministradorScreen(
    usuario: Usuario?,
    onCambiarTab: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    viewModel: AdministradorViewModel = viewModel()
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val resumen by viewModel.resumen.collectAsState()

    val fechaCabecera = remember {
        try {
            val hoy = LocalDate.now()
            val formato = DateTimeFormatter.ofPattern(
                "EEEE d 'de' MMMM",
                Locale("es", "CL")
            )
            hoy.format(formato).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "Hoy"
        }
    }

    val subtitulo = remember(usuario) {
        val nombre = usuario?.nombreCompleto ?: "Administración"
        val ciudad = usuario?.sede?.nombreCiudad ?: "Santiago"
        "$nombre · $ciudad"
    }

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            PanelBottomBar(
                rutaActual = Screen.PanelAdministrador.route,
                rutaPrincipal = Screen.PanelAdministrador.route,
                etiquetaPrincipal = "Admin",
                rutaPerfil = Screen.PerfilAdministrador.route,
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

            Text(
                text = fechaCabecera,
                color = LinceTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Panel de administración",
                    color = LinceTextPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onCerrarSesion) {
                    Text(
                        text = "Salir",
                        color = LinceTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = subtitulo,
                color = LinceTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TarjetaKpiAdmin(
                    valor = resumen.totalUsuarios,
                    etiqueta = "Usuarios",
                    color = LinceCyan,
                    modifier = Modifier.weight(1f)
                )
                TarjetaKpiAdmin(
                    valor = resumen.drivers,
                    etiqueta = "Drivers",
                    color = LincePending,
                    modifier = Modifier.weight(1f)
                )
                TarjetaKpiAdmin(
                    valor = resumen.guias,
                    etiqueta = "Guías",
                    color = LinceSuccess,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Acciones rápidas",
                color = LinceTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccionRapida(
                    icono = R.drawable.ic_person,
                    titulo = "Usuarios",
                    modifier = Modifier.weight(1f)
                )
                AccionRapida(
                    icono = R.drawable.ic_map,
                    titulo = "Catálogos",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccionRapida(
                    icono = R.drawable.ic_info,
                    titulo = "Reportes",
                    modifier = Modifier.weight(1f)
                )
                AccionRapida(
                    icono = R.drawable.ic_lock,
                    titulo = "Seguridad",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Cuentas del sistema",
                color = LinceTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(usuarios, key = { it.id }) { cuenta ->
                    FilaCuenta(cuenta)
                }
            }
        }
    }
}

@Composable
private fun TarjetaKpiAdmin(
    valor: Int,
    etiqueta: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = valor.toString(),
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = etiqueta,
                color = LinceTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AccionRapida(
    icono: Int,
    titulo: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LinceSurface,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = null,
                tint = LinceCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = titulo,
                color = LinceTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
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

private fun estiloRol(rol: RolUsuario): Triple<Color, Color, String> = when (rol) {
    RolUsuario.ADMINISTRADOR -> Triple(LinceRoleAdminSoft, LinceRoleAdmin, "Admin")
    RolUsuario.COORDINADOR -> Triple(LinceRoleCoordinatorSoft, LinceRoleCoordinator, "Coordinador")
    RolUsuario.DRIVER -> Triple(LinceRoleDriverSoft, LinceRoleDriver, "Driver")
    RolUsuario.GUIA -> Triple(LinceRoleGuideSoft, LinceRoleGuide, "Guía")
}

package com.pminteractive.pmtours.ui.screens.administrador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource // <-- IMPORT AÑADIDO
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.operadores.Administrador
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.ui.components.AdminBottomBar
import com.pminteractive.pmtours.ui.components.SelectorIdioma
import com.pminteractive.pmtours.ui.navigation.Screen
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.AdministradorViewModel

@Composable
fun PerfilAdministradorScreen(
    usuario: Usuario?,
    onCerrarSesion: () -> Unit = {},
    onCambiarTab: (String) -> Unit = {},
    viewModel: AdministradorViewModel = viewModel()
) {
    val resumen by viewModel.resumen.collectAsState()
    val nivelAcceso = (usuario as? Administrador)?.nivelAcceso ?: "SUPER_ADMIN"

    val iniciales = usuario?.nombreCompleto?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString() }
        ?.take(2)
        ?.joinToString("") ?: "AD"

    Scaffold(
        containerColor = LinceBackground,
        bottomBar = {
            AdminBottomBar(
                rutaActual = Screen.PerfilAdministrador.route,
                onNavegar = onCambiarTab
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(LinceSurface)
                        .border(2.dp, LinceCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iniciales,
                        color = LinceCyan,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = usuario?.nombreCompleto ?: stringResource(id = R.string.admin_perfil_nombre_fallback),
                        color = LinceTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(
                            id = R.string.admin_perfil_rol_base,
                            usuario?.rol?.name ?: "ADMINISTRADOR",
                            usuario?.sede?.nombreCiudad ?: "Santiago"
                        ),
                        color = LinceTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = stringResource(id = R.string.admin_perfil_nivel_acceso, nivelAcceso),
                        color = LinceTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(LinceCyan.copy(alpha = 0.5f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(id = R.string.admin_perfil_resumen_titulo),
                        color = LinceCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricaSistema(
                            valor = resumen.totalUsuarios,
                            etiqueta = stringResource(id = R.string.admin_perfil_metrica_usuarios),
                            color = LinceCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricaSistema(
                            valor = resumen.drivers,
                            etiqueta = stringResource(id = R.string.admin_perfil_metrica_drivers),
                            color = LincePending,
                            modifier = Modifier.weight(1f)
                        )
                        MetricaSistema(
                            valor = resumen.guias,
                            etiqueta = stringResource(id = R.string.admin_perfil_metrica_guias),
                            color = LinceSuccess,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = LinceSurface,
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(LinceSurfaceBorder)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.admin_perfil_contacto_titulo),
                        color = LinceCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(id = R.string.admin_perfil_contacto_correo, usuario?.correo ?: "admin@pmtours.cl"),
                        color = LinceTextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(id = R.string.admin_perfil_contacto_telefono, usuario?.telefono ?: "+56900001111"),
                        color = LinceTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = stringResource(id = R.string.admin_perfil_contacto_direccion, usuario?.direccion ?: "Oficina Central PM Tours, Santiago"),
                        color = LinceTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ==========================================
            // COMPONENTE DE SELECCIÓN DE IDIOMA
            // ==========================================
            SelectorIdioma()

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onCerrarSesion,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LinceError),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(LinceError)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.btn_cerrar_sesion), // Ya lo teníamos traducido de la otra pantalla
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricaSistema(
    valor: Int,
    etiqueta: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LinceBackground,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(LinceSurfaceBorder)
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = valor.toString(),
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = etiqueta,
                color = LinceTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
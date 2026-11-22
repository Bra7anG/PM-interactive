package com.pminteractive.pmtours.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // <-- IMPORT VITAL
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pminteractive.pmtours.R
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.network.NetworkMonitor
import com.pminteractive.pmtours.repository.AppContainer
import com.pminteractive.pmtours.ui.components.SelectorIdioma // <-- Agregamos el selector aquí también si quieres (opcional)
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.AuthUiState
import com.pminteractive.pmtours.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

private val USUARIOS_SIMPLES = setOf("driver", "guia", "coordinador", "admin")
private const val CLAVE_DEMO = "1234"

private enum class EstadoApiLogin {
    VERIFICANDO,
    EN_LINEA,
    SIN_INTERNET,
    API_NO_DISPONIBLE
}

@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        NetworkMonitor.inicializar(context)
    }

    val estaConectado by NetworkMonitor.estaConectado.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var intentoEnvio by remember { mutableStateOf(false) }
    var estadoApi by remember { mutableStateOf(EstadoApiLogin.VERIFICANDO) }
    var reintentoManual by remember { mutableIntStateOf(0) }

    LaunchedEffect(estaConectado, reintentoManual) {
        if (!estaConectado) {
            estadoApi = EstadoApiLogin.SIN_INTERNET
            return@LaunchedEffect
        }

        estadoApi = EstadoApiLogin.VERIFICANDO
        delay(350)

        val health = runCatching { AppContainer.apiService.getHealth() }.getOrNull()
        estadoApi = if (health?.estaOperativo == true) {
            EstadoApiLogin.EN_LINEA
        } else {
            EstadoApiLogin.API_NO_DISPONIBLE
        }
    }

    val entradaLimpia = usuario.trim()
    val esUsuarioSimple = entradaLimpia.lowercase() in USUARIOS_SIMPLES
    val esCodigoOperativo = entradaLimpia.uppercase().let {
        it.startsWith("TRA-") || it.startsWith("GUI-") || it.startsWith("CRD-") || it.startsWith("ADM-")
    }
    val usuarioInvalido = intentoEnvio && (
            entradaLimpia.isBlank() || (!entradaLimpia.contains("@") && !esUsuarioSimple && !esCodigoOperativo)
            )
    val contrasenaInvalida = intentoEnvio && (contrasena != CLAVE_DEMO)

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Autenticado) {
            val usuarioAutenticado = (uiState as AuthUiState.Autenticado).usuario
            onLoginExitoso(usuarioAutenticado)
        }
    }

    Scaffold(
        containerColor = LinceBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Logo superior corporativo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Lince PM Tours",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(105.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Títulos corporativos
            Text(
                text = stringResource(id = R.string.app_name), // Lee "PM TOURS" desde XML
                color = LinceTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.login_subtitulo), // Lee "Servicios turísticos Southbound" o su versión EN
                color = LinceTextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            IndicadorSaludServidor(
                estado = estadoApi,
                onReintentar = { reintentoManual++ }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Banner de error de credenciales
            if (uiState is AuthUiState.Error) {
                Surface(
                    color = LinceErrorContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LinceError),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("login_error")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠",
                            color = LinceError,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            // ==========================================
                            // AQUÍ ESTÁ EL ARREGLO PRINCIPAL DEL ERROR
                            // ==========================================
                            text = stringResource(id = (uiState as AuthUiState.Error).mensajeResId),
                            color = LinceError,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 4. Campo Usuario
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.login_label_usuario),
                    color = LinceTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        usuario = it
                        if (intentoEnvio) intentoEnvio = false
                    },
                    placeholder = {
                        Text(stringResource(id = R.string.login_hint_usuario), color = LinceTextSecondary.copy(alpha = 0.6f))
                    },
                    singleLine = true,
                    isError = usuarioInvalido,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LinceSurface,
                        unfocusedContainerColor = LinceSurface,
                        errorContainerColor = LinceSurface,
                        focusedTextColor = LinceTextPrimary,
                        unfocusedTextColor = LinceTextPrimary,
                        cursorColor = LinceCyan,
                        focusedBorderColor = LinceCyan,
                        unfocusedBorderColor = LinceSurfaceBorder,
                        errorBorderColor = LinceError
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_correo")
                )
                if (usuarioInvalido) {
                    Text(
                        text = stringResource(id = R.string.login_error_usuario),
                        color = LinceError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Campo Contraseña
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.login_label_clave),
                    color = LinceTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = {
                        contrasena = it
                        if (intentoEnvio) intentoEnvio = false
                    },
                    placeholder = {
                        Text(stringResource(id = R.string.login_hint_clave), color = LinceTextSecondary.copy(alpha = 0.6f))
                    },
                    singleLine = true,
                    isError = contrasenaInvalida,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LinceSurface,
                        unfocusedContainerColor = LinceSurface,
                        errorContainerColor = LinceSurface,
                        focusedTextColor = LinceTextPrimary,
                        unfocusedTextColor = LinceTextPrimary,
                        cursorColor = LinceCyan,
                        focusedBorderColor = LinceCyan,
                        unfocusedBorderColor = LinceSurfaceBorder,
                        errorBorderColor = LinceError
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_clave")
                )
                if (contrasenaInvalida) {
                    Text(
                        text = stringResource(id = R.string.login_error_clave),
                        color = LinceError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Botón de Acción Principal
            Button(
                onClick = {
                    focusManager.clearFocus()
                    intentoEnvio = true
                    if (!usuarioInvalido && !contrasenaInvalida && entradaLimpia.isNotBlank() && contrasena.isNotBlank()) {
                        viewModel.iniciarSesion(entradaLimpia, contrasena)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LinceCyan,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_btn")
            ) {
                Text(
                    text = stringResource(id = R.string.login_boton_ingresar),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 7. Selector de idioma directamente en el Login (opcional, pero buena práctica)
            SelectorIdioma()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IndicadorSaludServidor(
    estado: EstadoApiLogin,
    onReintentar: () -> Unit
) {
    // Leemos los strings nativos con una función para Compose
    val textoStr = when (estado) {
        EstadoApiLogin.VERIFICANDO -> stringResource(id = R.string.login_estado_verificando)
        EstadoApiLogin.EN_LINEA -> stringResource(id = R.string.login_estado_en_linea)
        EstadoApiLogin.SIN_INTERNET -> stringResource(id = R.string.login_estado_sin_internet)
        EstadoApiLogin.API_NO_DISPONIBLE -> stringResource(id = R.string.login_estado_caido)
    }

    val colorPunto = when (estado) {
        EstadoApiLogin.VERIFICANDO -> LinceTextSecondary
        EstadoApiLogin.EN_LINEA -> LinceSuccess
        EstadoApiLogin.SIN_INTERNET -> LincePending
        EstadoApiLogin.API_NO_DISPONIBLE -> LinceDanger
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onReintentar() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(colorPunto)
        )
        Text(
            text = textoStr,
            color = colorPunto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
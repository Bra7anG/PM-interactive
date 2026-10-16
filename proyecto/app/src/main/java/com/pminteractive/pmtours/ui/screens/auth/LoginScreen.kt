package com.pminteractive.pmtours.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pminteractive.pmtours.model.operadores.Usuario
import com.pminteractive.pmtours.ui.theme.*
import com.pminteractive.pmtours.viewmodel.AuthUiState
import com.pminteractive.pmtours.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var intentoEnvio by remember { mutableStateOf(false) }

    // Validaciones locales de los campos
    val correoInvalido = intentoEnvio && (correo.isBlank() || !correo.contains("@"))
    val contrasenaInvalida = intentoEnvio && (contrasena.length < 4)

    // Redirige automáticamente cuando el login es correcto
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Autenticado) {
            val usuario = (uiState as AuthUiState.Autenticado).usuario
            onLoginExitoso(usuario)
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

            // 1. Isotipo circular corporativo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(LinceSurface)
                    .border(2.dp, LinceSurfaceBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "L",
                    color = LinceCyan,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Títulos corporativos
            Text(
                text = "Lince",
                color = LinceTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Servicios turísticos Southbound",
                color = LinceTextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Banner de error si las credenciales fallan en el repositorio
            if (uiState is AuthUiState.Error) {
                Surface(
                    color = LinceErrorContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LinceError),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
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
                            text = (uiState as AuthUiState.Error).mensaje,
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
                    text = "Usuario",
                    color = LinceTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        if (intentoEnvio) intentoEnvio = false
                    },
                    placeholder = {
                        Text("carlos.morales@pmtours.cl", color = LinceTextSecondary.copy(alpha = 0.6f))
                    },
                    singleLine = true,
                    isError = correoInvalido,
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
                    modifier = Modifier.fillMaxWidth()
                )
                if (correoInvalido) {
                    Text(
                        text = "Ingrese un correo institucional válido",
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
                    text = "Contraseña",
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
                        Text("••••••••", color = LinceTextSecondary.copy(alpha = 0.6f))
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
                    modifier = Modifier.fillMaxWidth()
                )
                if (contrasenaInvalida) {
                    Text(
                        text = "La contraseña debe tener al menos 4 caracteres",
                        color = LinceError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Botón de Acción Principal (Cian)
            if (uiState is AuthUiState.Cargando) {
                CircularProgressIndicator(color = LinceCyan)
            } else {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        intentoEnvio = true
                        if (!correoInvalido && !contrasenaInvalida && correo.isNotBlank() && contrasena.isNotBlank()) {
                            viewModel.iniciarSesion(correo, contrasena)
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
                ) {
                    Text(
                        text = "Ingresar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 7. Nota inferior
            Text(
                text = "¿Olvidaste tu contraseña? — datos de prueba, sin credenciales reales",
                color = LinceTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
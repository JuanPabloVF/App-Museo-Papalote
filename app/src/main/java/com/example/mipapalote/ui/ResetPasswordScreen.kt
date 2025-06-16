package com.example.mipapalote.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ResetPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5) // Fondo claro neutro
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícono central superior
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Restablecer contraseña",
                tint = Color(0xFF8BC34A), // Verde para el ícono
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )

            // Título
            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFF2E7D32), // Verde oscuro
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Subtítulo
            Text(
                text = "Ingresa tu correo para recibir un enlace de restablecimiento",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp) // Ajuste para mayor centrado visual
                    .align(Alignment.CenterHorizontally),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, // Alineación central
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Input de correo con ícono
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Introduce tu correo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8BC34A),
                    focusedLabelColor = Color(0xFF8BC34A),
                    cursorColor = Color(0xFF8BC34A)
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock, // Ícono actualizado
                        contentDescription = "Correo",
                        tint = Color(0xFF8BC34A)
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de enviar correo de restablecimiento
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Correo de restablecimiento enviado", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login")
                                } else {
                                    val errorMessage = task.exception?.message ?: "Error desconocido"
                                    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Por favor, ingresa un correo válido", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Restablecer contraseña", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para regresar a la pantalla principal
            TextButton(onClick = { navController.navigate("login") }) {
                Text(
                    text = "Volver a la página principal",
                    color = Color(0xFF8BC34A),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                )
            }
        }
    }
}
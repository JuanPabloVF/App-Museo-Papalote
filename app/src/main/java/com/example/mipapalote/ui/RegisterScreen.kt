package com.example.mipapalote.ui

import android.content.ActivityNotFoundException
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

@Composable
fun RegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F5E9),
                        Color(0xFFE8F5E9)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Crea tu cuenta",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            )

            Text(
                "Ingresa tus datos",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8BC34A),
                            focusedLabelColor = Color(0xFF8BC34A)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color(0xFF8BC34A)
                            )
                        }
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8BC34A),
                            focusedLabelColor = Color(0xFF8BC34A)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Nombre",
                                tint = Color(0xFF8BC34A)
                            )
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Edad") },
                            modifier = Modifier.weight(0.45f), // Reduce el peso de este campo
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8BC34A),
                                focusedLabelColor = Color(0xFF8BC34A)
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Edad",
                                    tint = Color(0xFF8BC34A)
                                )
                            }
                        )

                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Sexo") },
                            modifier = Modifier.weight(0.55f), // Aumenta el peso de este campo
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8BC34A),
                                focusedLabelColor = Color(0xFF8BC34A)
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Sexo",
                                    tint = Color(0xFF8BC34A)
                                )
                            }
                        )
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                        },
                        label = { Text("Contraseña") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8BC34A),
                            focusedLabelColor = Color(0xFF8BC34A)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Contraseña",
                                tint = Color(0xFF8BC34A)
                            )
                        }
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            passwordError = false
                        },
                        label = { Text("Confirmar Contraseña") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8BC34A),
                            focusedLabelColor = Color(0xFF8BC34A)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Confirmar Contraseña",
                                tint = Color(0xFF8BC34A)
                            )
                        }
                    )

                    if (passwordError) {
                        Text(
                            text = "Las contraseñas no coinciden",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (password == confirmPassword) {
                        showPrivacyDialog = true
                    } else {
                        passwordError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8BC34A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Registrarse",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Ya tienes una cuenta?",
                    color = Color.Gray
                )
                TextButton(onClick = { navController.navigate("login") }) {
                    Text(
                        "Iniciar sesión",
                        color = Color(0xFF8BC34A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Aviso de Privacidad",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        "Al continuar, aceptas nuestro aviso de privacidad y términos de servicio.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://drive.google.com/file/d/1aFpaPcWTDMXmil5Bv36_sHbTVfWORtVj/view?usp=drive_link")
                                )
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "No se pudo abrir el enlace",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver aviso completo", color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPrivacyDialog = false
                        if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank() &&
                            age.isNotBlank() && gender.isNotBlank()) {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = task.result?.user?.uid
                                        if (userId != null) {
                                            val userData = mapOf(
                                                "name" to name,
                                                "age" to age,
                                                "gender" to gender,
                                                "email" to email
                                            )
                                            db.collection("users").document(userId).set(userData)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Registro exitoso",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.navigate("login") {
                                                        popUpTo("register") { inclusive = true }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error: ${e.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }
                                    } else {
                                        when (val exception = task.exception) {
                                            is FirebaseAuthWeakPasswordException ->
                                                Toast.makeText(context, "Contraseña demasiado débil", Toast.LENGTH_LONG).show()
                                            is FirebaseAuthInvalidCredentialsException ->
                                                Toast.makeText(context, "Correo inválido", Toast.LENGTH_LONG).show()
                                            is FirebaseAuthUserCollisionException ->
                                                Toast.makeText(context, "El usuario ya existe", Toast.LENGTH_LONG).show()
                                            else ->
                                                Toast.makeText(context, "Error: ${exception?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                        } else {
                            Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A))
                ) {
                    Text("Aceptar", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPrivacyDialog = false
                        navController.navigate("login")
                    },
                    border = BorderStroke(1.dp, Color(0xFF8BC34A))
                ) {
                    Text("Rechazar", color = Color(0xFF8BC34A))
                }
            }
        )
    }
}

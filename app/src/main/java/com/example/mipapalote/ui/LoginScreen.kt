package com.example.mipapalote.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mipapalote.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Google Sign-In configuration
    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                isLoading = true
                firebaseAuthWithGoogle(account, auth) { success ->
                    isLoading = false
                    if (!success) {
                        Toast.makeText(context, "Error al iniciar sesión con Google", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: ApiException) {
                isLoading = false
            }
        }
    )

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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_museo),
                contentDescription = "Logo del Museo",
                modifier = Modifier
                    .size(140.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "¡Bienvenido!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF2E7D32)
            )

            Text(
                "Ingresa a tu cuenta",
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
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8BC34A),
                            focusedLabelColor = Color(0xFF8BC34A)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = "Email Icon",
                                tint = Color(0xFF8BC34A)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8BC34A),
                            focusedLabelColor = Color(0xFF8BC34A)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Password Icon",
                                tint = Color(0xFF8BC34A)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (showPassword) android.R.drawable.ic_menu_view
                                        else android.R.drawable.ic_secure
                                    ),
                                    contentDescription = if (showPassword) "Ocultar contraseña"
                                    else "Mostrar contraseña",
                                    tint = Color(0xFF8BC34A)
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        loginUser(email, password, auth, db, navController, context) {
                            isLoading = false
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Por favor, ingresa todos los campos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8BC34A),
                    contentColor = Color.White
                ),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Iniciar sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "O continúa con",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            OutlinedButton(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.DarkGray
                ),
                border = BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Iniciar sesión con Google",
                        fontSize = 16.sp
                    )
                }
            }

            TextButton(
                onClick = { navController.navigate("resetPassword") },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = Color(0xFF8BC34A),
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿No tienes una cuenta?",
                    color = Color.Gray
                )
                TextButton(onClick = { navController.navigate("register") }) {
                    Text(
                        "Crear cuenta",
                        color = Color(0xFF8BC34A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF8BC34A))
            }
        }
    }
}

fun firebaseAuthWithGoogle(
    account: GoogleSignInAccount?,
    auth: FirebaseAuth,
    onComplete: (Boolean) -> Unit
) {
    if (account == null) {
        onComplete(false)
        return
    }

    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
}

private fun loginUser(
    email: String,
    password: String,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    navController: NavController,
    context: android.content.Context,
    onComplete: () -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                checkAdminStatus(email, db, navController, context, onComplete)
            } else {
                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                onComplete()
            }
        }
}

private fun checkAdminStatus(
    email: String,
    db: FirebaseFirestore,
    navController: NavController,
    context: android.content.Context,
    onComplete: () -> Unit
) {
    db.collection("admins").document(email).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                navigateTo(navController, "adminDashboard", context)
            } else {
                navigateTo(navController, "home", context)
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error checking user status: ${e.message}", Toast.LENGTH_LONG).show()
        }
        .addOnCompleteListener {
            onComplete()
        }
}

private fun navigateTo(navController: NavController, route: String, context: android.content.Context) {
    try {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
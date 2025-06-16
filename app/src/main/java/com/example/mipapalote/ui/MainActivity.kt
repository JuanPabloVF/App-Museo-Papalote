package com.example.mipapalote.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mipapalote.ui.admin.AdminStatistics
import com.example.mipapalote.ui.theme.MiPapaloteTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Configure full-screen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        super.onCreate(savedInstanceState)

        // Firebase Initialization
        Firebase.initialize(applicationContext)
        Log.d("FirebaseInit", "Firebase initialized successfully")

        setContent {
            MiPapaloteTheme {
                MiPapaloteApp()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MiPapaloteApp() {
    val navController = rememberNavController()
    val userProfileViewModel = remember { UserProfileViewModel() }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var isAdmin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.let { user ->
            db.collection("admins").document(user.email ?: "").get()
                .addOnSuccessListener { document ->
                    isAdmin = document.exists()
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } ?: run {
            isLoading = false
        }
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute !in listOf("login", "register", "resetPassword") && !isLoading) {
                BottomNavigationBar(navController = navController, isAdmin = isAdmin)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("resetPassword") { ResetPasswordScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("feedback") { FeedbackScreen(userProfileViewModel) }
            composable("adminDashboard") { AdminDashboard(navController) }
            composable("adminStatistics") { AdminStatistics() }
            composable("dashboard") {
                DashboardScreen(navController, false) // Para usuarios regulares
            }
            composable("home") { HomeScreen(navController) }
            composable("QR") { QRScreen(navController) }

            // Usamos solo esta ruta para pasar exhibitId a Dashboard y mostrar detalles
            composable("dashboard/{exhibitId}") { backStackEntry ->
                val exhibitId = backStackEntry.arguments?.getString("exhibitId")
                if (exhibitId != null) {
                    DashboardScreen(navController, false)
                } else {
                    Text("Exhibit ID is missing")
                }
            }
        }
    }
}
package com.example.mipapalote.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mipapalote.ui.theme.greenPrimary

@Composable
fun BottomNavigationBar(navController: NavController, isAdmin: Boolean) {
    val items = if (isAdmin) {
        // Opciones para administradores
        listOf(Screen.AdminDashboard, Screen.Statistics, Screen.Feedback, Screen.Profile)
    } else {
        // Opciones para usuarios normales
        listOf(Screen.Home, Screen.Dashboard, Screen.QR, Screen.Feedback, Screen.Profile)
    }

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = Color(0xFFF5F5E9),
        tonalElevation = 5.dp
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(text = screen.title) },
                selected = currentRoute == screen.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = greenPrimary,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = greenPrimary,
                    unselectedTextColor = Color.Gray
                ),
                onClick = {
                    // Asegúrate de que la pantalla de Dashboard no se apile innecesariamente
                    navController.navigate(screen.route) {
                        // Evita duplicados y regresa correctamente a las pantallas anteriores
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
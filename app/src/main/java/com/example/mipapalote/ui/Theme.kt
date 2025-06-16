// Theme.kt
package com.example.mipapalote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define los colores de la paleta
val Pantone382C = Color(0xFFBAD636)      // Pertenencia (verde claro)
val Pantone307C = Color(0xFF0076BE)      // Comunicación (azul)
val Pantone2593C = Color(0xFF7A1FA2)     // Comprensión (morado)
val Pantone199C = Color(0xFFD50032)      // Identidad (rojo)
val Pantone151C = Color(0xFFFF8200)      // Expresión (naranja)
val Pantone320C = Color(0xFF008EAA)      // "Pequeños Mar" (azul turquesa)
val Pantone369C = Color(0xFF43B02A)      // "Pequeños Bosque" (verde)

val greenPrimary = Pantone382C  // Define el color verde principal

// Esquema de colores claros
private val LightColors = lightColorScheme(
    primary = greenPrimary,
    onPrimary = Color.White,
    secondary = Pantone307C,
    onSecondary = Color.White,
    background = Color.White,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// Esquema de colores oscuros
private val DarkColors = darkColorScheme(
    primary = Pantone2593C,
    onPrimary = Color.Black,
    secondary = Pantone199C,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MiPapaloteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
package com.example.mipapalote.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mipapalote.R
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size



@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xcde5aff)) // Cambiar el color de fondo solo para HomeScreen
        ) {
            // Pass navController to MapSlider here
            MapSlider(modifier = Modifier.padding(innerPadding), navController = navController)
        }
    }
}

@Composable
fun MapSlider(modifier: Modifier = Modifier, navController: NavController) {
    var selectedMap by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var zoneName by remember { mutableStateOf("") }

    val images = listOf(
        R.drawable.museo_pb, // Imagen de la planta baja
        R.drawable.museo_pa  // Imagen de la planta alta
    )

    val plantNames = listOf("Planta Baja", "Planta Alta")

    // Coordenadas relativas para los botones de zonas
    val buttonPositions = listOf(
        listOf(
            Pair(0f, 0f), // Rojo
            Pair(0.07f, 0.0f), // Zona Turquesa
            Pair(0.06f, -0.05f), // Zona Naranja
            Pair(0f, -0.076f) // Zona Morado
        ),
        listOf(
            Pair(-0.009f, 0.03f), // Zona Azul Marino
            Pair(0.055f, 0.039f),
            Pair(0.05f, -0.013f) // Zona Verde
        )
    )

    val zonesByPlant = listOf(
        listOf(
            "Soy" to Color.Red,
            "Pequeños" to Color.Cyan,
            "Expreso" to Color(0xFFFFA500),
            "Comprendo" to Color(0xFF800080)
        ),
        listOf(
            "Comunico" to Color.Blue,
            "Pequeños" to Color.Cyan,
            "Pertenezco" to Color.Green
        )
    )

    // Servicios asignados según la planta
    val servicesByPlant = listOf(
        listOf("2. Escaleras"), // Planta baja
        listOf(
            "2. Escaleras",
            "8. Sillas de ruedas y carriolas",
            "6. Enfermería",
            "5. Mesa de informes",
            "3. Elevador",
            "4. Taquilla",
            "7. Bebederos"
        ) // Planta alta
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botones para cambiar entre plantas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            plantNames.forEachIndexed { index, name ->
                Button(
                    onClick = { selectedMap = index },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMap == index) MaterialTheme.colorScheme.primary else Color.White,
                        contentColor = if (selectedMap == index) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(text = name)
                }
            }
        }

        // Servicios arriba y zonas debajo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Column para los servicios
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Servicios",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                servicesByPlant[selectedMap].forEach { service ->
                    Text(
                        text = "• $service",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Column para las zonas
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Zonas",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                zonesByPlant[selectedMap].forEach { (zoneName, color) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(color = color, shape = CircleShape)
                                .border(1.dp, Color.Black, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = zoneName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Empuja el mapa hacia abajo

        // Mostrar mapa y botones interactivos
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp), // Ajusta la altura del mapa según sea necesario
            contentAlignment = Alignment.Center
        ) {
            val imageWidth = constraints.maxWidth.toFloat()
            val imageHeight = constraints.maxHeight.toFloat()

            Image(
                painter = painterResource(id = images[selectedMap]),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            buttonPositions[selectedMap].forEachIndexed { index, position ->
                val absoluteX = position.first * imageWidth
                val absoluteY = position.second * imageHeight

                Box(
                    modifier = Modifier
                        .offset(x = absoluteX.dp, y = absoluteY.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(zonesByPlant[selectedMap][index].second.copy(alpha = 0.7f))
                        .border(2.dp, Color.Black, CircleShape)
                        .clickable {
                            zoneName = zonesByPlant[selectedMap][index].first
                            showDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.click),
                        contentDescription = "Click Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    if (showDialog) {
        // Pass navController here to ZoneDialog
        ZoneDialog(zoneName = zoneName, onDismiss = { showDialog = false }, navController = navController)
    }
}

@Composable
fun ZoneDialog(zoneName: String, onDismiss: () -> Unit, navController: NavController) {
    // Descripciones breves de cada zona
    val descriptions = mapOf(
        "Comprendo" to "Comprendo cómo funciona mi planeta y cómo cuidarlo a través de la ciencia.",
        "Comunico" to "Comunico mis ideas para mejorar el medio ambiente.",
        "Pequeños" to "Exploro la naturaleza a través de mis sentidos.",
        "Soy" to "Soy consciente de que mis decisiones pueden dañar o mejorar el medio ambiente.",
        "Pertenezco" to "Pertenezco a una gran red de vida en la que todo se relaciona para funcionar",
        "Expreso" to "Expreso mis ideas para que otras personas las entiendan."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Información de la zona: $zoneName")
        },
        text = {
            Text(text = descriptions[zoneName] ?: "Descripción no disponible para esta zona.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss() // Cierra el diálogo
                val exhibitId = getExhibitIdForZone(zoneName)

                // Asegurarnos de que exhibitId no sea nulo o vacío
                if (exhibitId.isNotEmpty()) {
                    // Realizar navegación con el ID de la exhibición
                    Log.d("ExhibitNavigation", "Navigating to exhibit with ID: $exhibitId")
                    navController.navigate("dashboard/$exhibitId") {
                        // Evitar duplicados y actualizar correctamente la barra inferior
                        popUpTo("home") { inclusive = false }  // Regresa al home sin eliminarlo de la pila
                        launchSingleTop = true  // Solo lanzará una nueva instancia si no existe
                        restoreState = true  // Restaura el estado de las pantallas anteriores
                    }
                } else {
                    Log.e("Navigation Error", "Exhibit ID not found for zone: $zoneName")
                }
            }) {
                Text("Ver más")
            }
        }
    )
}


fun getExhibitIdForZone(zoneName: String): String {
    // Mapeo de zonas a las ID de las exhibiciones correspondientes
    return when (zoneName) {
        "Soy" -> "2FyGXzekYHj6u8PNF31T"  // Reemplazar con el ID real de la exhibición Soy
        "Pequeños" -> "ekiNAqhUibDXnLrUmd4N"  // Reemplazar con el ID real de la exhibición Pequeños
        "Expreso" -> "9zdafVz8PBII9aZbqufY"  // Reemplazar con el ID real de la exhibición Expreso
        "Comprendo" -> "GrhqVAFqUtQeYwMxntuQ"  // Reemplazar con el ID real de la exhibición Comprendo
        "Pertenezco" -> "Pertenezo-Estratos"  // Reemplazar con el ID real de la exhibición Pertenezco
        "Comunico" -> "GpoE6ExfRN681nmRCeNB"  // Reemplazar con el ID real de la exhibición Comunico
        else -> "default_exhibit_id"  // ID predeterminada en caso de que no haya coincidencia
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}
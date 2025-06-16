package com.example.mipapalote.ui

import android.content.Context
import android.content.Intent
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage

data class Exhibit(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val message: String = "",
    val objectives: List<String> = emptyList(),
    val questions: List<String> = emptyList(),
    val rating: Float = 0f,
    val zone: String = "",
    val area: String = "", // Nuevo campo para área
    val curator: String = "",
    val duration: String = "",
    val imageUrl: String = "" // Asegúrate de que este campo sea una cadena base64
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, isAdmin: Boolean = false) {
    val exhibits = remember { mutableStateListOf<Exhibit>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState() // Estado del desplazamiento
    val coroutineScope = rememberCoroutineScope() // Alcance para desplazamiento

    // Cargar los datos desde Firestore
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("exhibits").get()
            .addOnSuccessListener { snapshot ->
                exhibits.clear()
                snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Exhibit(
                            id = doc.id,
                            name = doc.getString("name") ?: "Sin nombre",
                            description = doc.getString("description") ?: "Sin descripción",
                            message = doc.getString("message") ?: "Sin mensaje",
                            objectives = (doc["objectives"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            questions = (doc["questions"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                            zone = doc.getString("thematicZone") ?: "Sin zona",
                            area = doc.getString("area") ?: "Sin área", // Extrae el área desde Firebase
                            curator = doc.getString("curator") ?: "Unknown curator",
                            duration = doc.getString("duration") ?: "30 minutos",
                            imageUrl = doc.getString("imageUrl") ?: "" // Cadena base64 de la imagen
                        )
                    } catch (e: Exception) {
                        null
                    }
                }?.let { exhibits.addAll(it) }
                isLoading = false
            }
            .addOnFailureListener {
                errorMessage = "Error al cargar exhibiciones"
                isLoading = false
            }
    }

    // Interfaz de usuario principal
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Exhibiciones disponibles", fontSize = 20.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage!!, color = Color.Red)
                }
            } else if (exhibits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay exhibiciones disponibles", color = Color.Gray)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFFF5F5F5))
                        .padding(top = 0.dp) // Eliminando todo el espacio superior
                ) {
                    // Selector de exhibiciones
                    ExhibitSelector(exhibits) { selectedExhibit ->
                        val index = exhibits.indexOf(selectedExhibit)
                        if (index >= 0) {
                            coroutineScope.launch {
                                listState.scrollToItem(index)
                            }
                        }
                    }

                    // Lista de todas las exhibiciones como tarjetas
                    LazyColumn(
                        state = listState, // Usa el estado de desplazamiento aquí
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(exhibits) { exhibit ->
                            ExhibitCard(exhibit)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExhibitSelector(exhibits: List<Exhibit>, onExhibitSelected: (Exhibit) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedExhibitName by remember { mutableStateOf("Selecciona una exhibición") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = selectedExhibitName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor() // Para que se ancle correctamente
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            exhibits.forEach { exhibit ->
                DropdownMenuItem(
                    text = { Text(exhibit.name) },
                    onClick = {
                        onExhibitSelected(exhibit)
                        selectedExhibitName = exhibit.name
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ExhibitCard(exhibit: Exhibit, onCardClick: () -> Unit = {}) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val cardElevation by animateDpAsState(
        targetValue = if (isExpanded) 8.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable {
                isExpanded = !isExpanded
                onCardClick()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF0F4F8),
                            Color.White
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header with exhibit name, location, and share button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exhibit.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A2B3C)
                    )
                )

                // Share Button
                IconButton(onClick = { shareExhibit(context, exhibit) }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Compartir exhibición",
                        tint = Color(0xFF2C7CB0)
                    )
                }
            }

            // Location Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFE6F2FF), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    tint = Color(0xFF2C7CB0),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${exhibit.zone} - ${exhibit.area}",
                    color = Color(0xFF2C7CB0),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Exhibit Image Handling
            if (exhibit.imageUrl.isNotEmpty()) {
                val imageBitmap = remember(exhibit.imageUrl) {
                    decodeBase64ToImage(exhibit.imageUrl)?.asImageBitmap()
                }

                imageBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = exhibit.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp),
                        contentScale = ContentScale.Crop
                    )
                } ?: Text(
                    "No se pudo cargar la imagen",
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Rating and Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Calificación",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${exhibit.rating} / 5.0",
                        color = Color(0xFF455A64),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Duration
                Text(
                    text = "Duración: ${exhibit.duration}",
                    color = Color(0xFF607D8B),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Expandable Details
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                // Exhibit Description
                Text(
                    "Descripción:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                )
                Text(
                    exhibit.description,
                    color = Color(0xFF546E7A),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Objectives
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Objetivos:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                )
                exhibit.objectives.forEach { objective ->
                    Text(
                        "• $objective",
                        color = Color(0xFF546E7A),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                // Questions
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Preguntas:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                )
                exhibit.questions.forEach { question ->
                    Text(
                        "• $question",
                        color = Color(0xFF546E7A),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// Function to decode Base64 string to ImageBitmap
// Robust Base64 image decoding
fun decodeBase64ToImage(base64String: String): Bitmap? {
    return try {
        val cleanBase64 = base64String.replace("data:image/.*;base64,", "")
        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

// Enhanced sharing function
fun shareExhibit(context: Context, exhibit: Exhibit) {
    val shareText = """
        Mira esta increíble exhibición del Papalote Museo Del Niño!
        🏛️ Exhibición: ${exhibit.name}
        
        Descripción: ${exhibit.description}
        Zona: ${exhibit.zone}
        Duración: ${exhibit.duration}
        Calificación: ${exhibit.rating}/5.0
        
        ¡No te pierdas esta increíble exhibición!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Exhibición interesante")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir exhibición"))
}

@Composable
@Preview
fun PreviewExhibitCard() {
    ExhibitCard(
        Exhibit(
            name = "Exhibición de ejemplo",
            description = "Descripción de la exhibición.",
            imageUrl = "base64_string_de_ejemplo",  // Debes colocar un string base64 aquí
            rating = 5.0f,
            zone = "Zona 1",
            objectives = listOf("Objetivo 1", "Objetivo 2"),
            questions = listOf("Pregunta 1", "Pregunta 2")
        )
    )
}

@Composable
fun ExhibitDetailsScreen(navController: NavController, exhibitId: String) {
    val db = FirebaseFirestore.getInstance()
    var exhibit by remember { mutableStateOf<Exhibit?>(null) }

    LaunchedEffect(exhibitId) {
        db.collection("exhibits").document(exhibitId).get()
            .addOnSuccessListener { document ->
                exhibit = document.toObject(Exhibit::class.java)
            }
            .addOnFailureListener {
                exhibit = null // Si no se encuentra la exhibición, manejar el error aquí
            }
    }

    if (exhibit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Detalles de: ${exhibit?.name}", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Zona: ${exhibit?.zone}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Curador: ${exhibit?.curator}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Descripción: ${exhibit?.description}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}




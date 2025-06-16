package com.example.mipapalote.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(navController: NavController) {
    val (exhibitName, setExhibitName) = remember { mutableStateOf("") }
    val (thematicZone, setThematicZone) = remember { mutableStateOf("") }
    val (message, setMessage) = remember { mutableStateOf("") }
    val (objectives, setObjectives) = remember { mutableStateOf("") }
    val (questions, setQuestions) = remember { mutableStateOf("") }
    val (area, setArea) = remember { mutableStateOf("") }
    val (description, setDescription) = remember { mutableStateOf("") }
    val (rating, setRating) = remember { mutableStateOf("") }
    val (imageUri, setImageUri) = remember { mutableStateOf<Uri?>(null) }
    val (snackbarMessage, setSnackbarMessage) = remember { mutableStateOf("") }
    val (isEditing, setIsEditing) = remember { mutableStateOf(false) }
    val (currentExhibitId, setCurrentExhibitId) = remember { mutableStateOf<String?>(null) }

    val firestore = FirebaseFirestore.getInstance()
    val exhibits = remember { mutableStateListOf<Map<String, Any>>() }

    // Cargar las exhibiciones al inicio
    LaunchedEffect(Unit) {
        loadExhibits(firestore, exhibits) { message ->
            setSnackbarMessage(message)
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        setImageUri(uri) // Guardar URI de imagen seleccionada
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard - Manage Exhibitions") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                if (isEditing) "Edit Exhibit" else "Add New Exhibit",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Campos del formulario
            TextField(value = exhibitName, onValueChange = setExhibitName, label = { Text("Exhibition Name") }, modifier = Modifier.fillMaxWidth())
            TextField(value = thematicZone, onValueChange = setThematicZone, label = { Text("Thematic Zone") }, modifier = Modifier.fillMaxWidth())
            TextField(value = message, onValueChange = setMessage, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
            TextField(value = objectives, onValueChange = setObjectives, label = { Text("Objectives (comma-separated)") }, modifier = Modifier.fillMaxWidth())
            TextField(value = questions, onValueChange = setQuestions, label = { Text("Questions (comma-separated)") }, modifier = Modifier.fillMaxWidth())
            TextField(value = area, onValueChange = setArea, label = { Text("Area") }, modifier = Modifier.fillMaxWidth())
            TextField(value = description, onValueChange = setDescription, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            TextField(value = rating, onValueChange = setRating, label = { Text("Rating (1 to 5)") }, modifier = Modifier.fillMaxWidth())

            // Selección de imagen
            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text("Select Image")
            }

            // Vista previa de imagen seleccionada
            imageUri?.let {
                Text("Selected Image:")
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Botón para guardar la exhibición
            Button(
                onClick = {
                    val exhibitData = mapOf(
                        "name" to exhibitName,
                        "thematicZone" to thematicZone,
                        "message" to message,
                        "objectives" to objectives.split(",").map { it.trim() },
                        "questions" to questions.split(",").map { it.trim() },
                        "area" to area,
                        "description" to description,
                        "rating" to (rating.toFloatOrNull() ?: 0f),
                    )

                    if (isEditing && currentExhibitId != null) {
                        // Si estamos editando
                        if (imageUri != null) {
                            // Convertir la imagen a base64 y agregarla a los datos
                            val imageBase64 = convertImageToBase64(imageUri, navController.context)

// Convertir la imagen a base64 y agregarla a los datos
                            val updatedExhibitData = exhibitData + ("imageUrl" to imageBase64 as Any)
                            updateExhibit(
                                firestore,
                                currentExhibitId,
                                updatedExhibitData,
                                onSuccess = {
                                    setSnackbarMessage("Exhibition updated successfully!")
                                    resetForm()
                                    setIsEditing(false)
                                },
                                onError = { errorMessage ->
                                    setSnackbarMessage("Error updating exhibition: $errorMessage")
                                }
                            )
                        } else {
                            // Actualizar sin cambiar la imagen
                            updateExhibit(
                                firestore,
                                currentExhibitId!!,
                                exhibitData,
                                onSuccess = {
                                    setSnackbarMessage("Exhibition updated successfully!")
                                    resetForm()
                                    setIsEditing(false)
                                },
                                onError = { errorMessage ->
                                    setSnackbarMessage("Error updating exhibition: $errorMessage")
                                }
                            )
                        }
                    } else if (imageUri != null) {
                        // Convertir la imagen a base64 y agregarla a los datos
                        val imageBase64 = convertImageToBase64(imageUri, navController.context)

                        // Asegurarse de que el valor sea de tipo Any
                        val updatedExhibitData = exhibitData + ("imageUrl" to imageBase64 as Any)

                        addExhibit(
                            firestore,
                            updatedExhibitData, // Usar los datos actualizados con la imagen base64
                            onSuccess = {
                                setSnackbarMessage("Exhibition added successfully!")
                                resetForm()
                            },
                            onError = { errorMessage ->
                                setSnackbarMessage("Error adding exhibition: $errorMessage")
                            }
                        )
                    }else {
                        setSnackbarMessage("Please select an image.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Update Exhibition" else "Add Exhibition")
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color.Gray)

            Text("Existing Exhibitions", style = MaterialTheme.typography.headlineSmall)

            // Lista de exhibiciones
            exhibits.forEach { exhibit ->
                AdminExhibitCard(
                    exhibit,
                    onEdit = {
                        loadFormForEditing(
                            exhibit,
                            setExhibitName,
                            setArea,
                            setDescription,
                            setRating,
                            setQuestions,
                            setCurrentExhibitId,
                            setIsEditing
                        )
                    },
                    onDelete = {
                        deleteExhibit(firestore, exhibit["id"] as String, exhibits) { message ->
                            setSnackbarMessage(message)
                        }
                    }
                )
            }

            // Mostrar Snackbar
            if (snackbarMessage.isNotEmpty()) {
                Snackbar(
                    action = {
                        Button(onClick = { setSnackbarMessage("") }) {
                            Text("Dismiss")
                        }
                    }
                ) { Text(snackbarMessage) }
            }
        }
    }
}

// Función para convertir la imagen seleccionada a Base64
fun convertImageToBase64(imageUri: Uri, context: Context): String? {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

@Composable
fun AdminExhibitCard(
    exhibit: Map<String, Any>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exhibit["name"] as? String ?: "Unnamed Exhibit",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Area: ${exhibit["area"] as? String ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Rating: ${(exhibit["rating"] as? Double)?.toString() ?: "Not Rated"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

private fun loadExhibits(
    firestore: FirebaseFirestore,
    exhibits: MutableList<Map<String, Any>>,
    onError: (String) -> Unit
) {
    firestore.collection("exhibits").get()
        .addOnSuccessListener { snapshot ->
            exhibits.clear()
            exhibits.addAll(snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) })
        }
        .addOnFailureListener { exception ->
            val errorMessage = when (exception.message) {
                "PERMISSION_DENIED" -> "No tienes permisos para cargar las exhibiciones."
                else -> "Error al cargar las exhibiciones: ${exception.message}"
            }
            onError(errorMessage)
        }
}

private fun addExhibit(
    firestore: FirebaseFirestore,
    data: Map<String, Any>,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    firestore.collection("exhibits").add(data)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(e.message ?: "Unknown error") }
}

private fun updateExhibit(
    firestore: FirebaseFirestore,
    exhibitId: String,
    data: Map<String, Any>,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    firestore.collection("exhibits").document(exhibitId).update(data)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(e.message ?: "Unknown error") }
}

private fun deleteExhibit(
    firestore: FirebaseFirestore,
    exhibitId: String,
    exhibits: MutableList<Map<String, Any>>,
    onSuccess: (String) -> Unit
) {
    firestore.collection("exhibits").document(exhibitId).delete()
        .addOnSuccessListener {
            exhibits.removeIf { it["id"] == exhibitId }
            onSuccess("Exhibit deleted successfully!")
        }
        .addOnFailureListener { e ->
            onSuccess("Error deleting exhibit: ${e.message}")
        }
}

private fun loadFormForEditing(
    exhibit: Map<String, Any>,
    setExhibitName: (String) -> Unit,
    setArea: (String) -> Unit,
    setDescription: (String) -> Unit,
    setRating: (String) -> Unit,
    setQuestions: (String) -> Unit,
    setCurrentExhibitId: (String?) -> Unit,
    setIsEditing: (Boolean) -> Unit
) {
    setExhibitName(exhibit["name"] as? String ?: "")
    setArea(exhibit["area"] as? String ?: "")
    setDescription(exhibit["description"] as? String ?: "")
    setRating((exhibit["rating"] as? Double)?.toString() ?: "")
    setQuestions((exhibit["questions"] as? List<*>)?.joinToString(", ") ?: "")
    setCurrentExhibitId(exhibit["id"] as? String)
    setIsEditing(true)
}

fun resetForm() {
        // Reset all form fields to their default state
}

package com.example.mipapalote.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Data class para representar los comentarios
data class Comment(
    var id: String,
    val userId: String,
    val username: String,
    var comment: String,
    val time: String,
    val profileImageBase64: String?,
    var rating: Int = 0,
    var reply: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(userProfileViewModel: UserProfileViewModel = UserProfileViewModel()) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userName by userProfileViewModel.userName.collectAsState()
    val currentUserId = auth.currentUser?.uid ?: ""
    val comments = remember { mutableStateListOf<Comment>() }
    var averageRating by remember { mutableStateOf(0.0) }
    var isAdmin by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var selectedComment by remember { mutableStateOf<Comment?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editCommentText by remember { mutableStateOf("") }
    var selectedRating by remember { mutableStateOf(0) }

    val db = FirebaseFirestore.getInstance()

    // Recalcular el promedio de las calificaciones
    fun recalculateAverageRating() {
        val totalRating = comments.sumOf { it.rating }
        val count = comments.size
        averageRating = if (count > 0) totalRating.toDouble() / count else 0.0
    }

    // Cargar comentarios y verificar si el usuario es administrador
    LaunchedEffect(Unit) {
        userProfileViewModel.reloadUserName()

        val email = auth.currentUser?.email
        if (!email.isNullOrEmpty()) {
            db.collection("admins").document(email).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        isAdmin = true
                    } else {
                        isAdmin = false
                    }
                }
        }
        db.collection("comments").get()
            .addOnSuccessListener { result ->
                comments.clear()
                for (document in result) {
                    val id = document.id
                    val userId = document.getString("userId") ?: ""
                    val username = document.getString("username") ?: "Usuario"
                    val comment = document.getString("comment") ?: ""
                    val timestamp = document.getTimestamp("timestamp") ?: Timestamp.now()
                    val profileImageBase64 = document.getString("profileImageBase64")
                    val rating = document.getLong("rating")?.toInt() ?: 0
                    val reply = document.getString("reply") ?: ""
                    val timeText = getRelativeTimeSpan(timestamp.toDate())
                    comments.add(
                        Comment(
                            id = id,
                            userId = userId,
                            username = username,
                            comment = comment,
                            time = timeText,
                            profileImageBase64 = profileImageBase64,
                            rating = rating,
                            reply = reply
                        )
                    )
                }
                recalculateAverageRating()
            }
    }

    // Agregar respuesta al comentario seleccionado
    fun addReplyToComment() {
        if (replyText.isNotBlank() && selectedComment != null) {
            val updateData = mapOf("reply" to replyText.trim()) // Solo enviamos 'reply'
            db.collection("comments").document(selectedComment!!.id)
                .update(updateData)
                .addOnSuccessListener {
                    selectedComment?.reply = replyText.trim()
                    replyText = ""
                    selectedComment = null
                    Toast.makeText(context, "Respuesta enviada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Error al enviar respuesta: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comentarios") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedComment = null // Limpiar el comentario seleccionado
                    editCommentText = "" // Limpiar el texto del comentario
                    showEditDialog = true // Mostrar el diálogo para agregar un nuevo comentario
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Agregar comentario")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                if (comments.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "%.1f".format(averageRating),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.align(Alignment.Top)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Estrella $i",
                                        tint = if (i <= averageRating) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(comments) { comment ->
                        CommentItem(
                            comment = comment,
                            isAdmin = isAdmin,
                            currentUserId = currentUserId,
                            onEdit = {
                                selectedComment = comment
                                editCommentText =
                                    comment.comment // Prellenar el texto del comentario actual
                                showEditDialog = true
                            },
                            onDelete = {
                                // Acción para eliminar el comentario
                                deleteComment(
                                    comment = comment,
                                    db = db,
                                    onSuccess = {
                                        comments.remove(comment)
                                        Toast.makeText(
                                            context,
                                            "Comentario eliminado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFailure = { errorMessage ->
                                        Toast.makeText(
                                            context,
                                            "Error al eliminar: $errorMessage",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            },
                            onReply = {
                                // Acción para responder al comentario
                                selectedComment = comment
                                replyText = ""
                            }
                        )
                        Divider()
                    }
                }

                // Diálogo para editar o agregar un nuevo comentario
                if (showEditDialog) {
                    AlertDialog(
                        onDismissRequest = { showEditDialog = false },
                        title = { Text(if (selectedComment == null) "Agregar comentario" else "Editar comentario") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = editCommentText,
                                    onValueChange = { editCommentText = it },
                                    label = { Text("Comentario") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (selectedComment == null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        for (i in 1..5) {
                                            IconButton(
                                                onClick = { selectedRating = i },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = "Calificación $i",
                                                    tint = if (i <= selectedRating) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.4f
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (selectedComment == null) {
                                    // Agregar un nuevo comentario
                                    val newComment = hashMapOf(
                                        "userId" to currentUserId,
                                        "username" to userName,
                                        "comment" to editCommentText.trim(), // Asegúrate de que no esté vacío
                                        "time" to Timestamp.now(),
                                        "profileImageBase64" to null, // O un valor válido si lo tienes
                                        "rating" to selectedRating
                                    )

                                    db.collection("comments").add(newComment)
                                        .addOnSuccessListener { document ->
                                            // Crear un objeto Comment localmente
                                            val addedComment = Comment(
                                                id = document.id,
                                                userId = currentUserId,
                                                username = userName,
                                                comment = editCommentText.trim(),
                                                time = "Hace un momento",
                                                profileImageBase64 = null,
                                                rating = selectedRating
                                            )
                                            comments.add(0, addedComment) // Agregar a la lista local
                                            recalculateAverageRating() // Recalcular el promedio
                                            Toast.makeText(context, "Comentario agregado", Toast.LENGTH_SHORT).show()
                                            showEditDialog = false // Cerrar el diálogo
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Error al agregar comentario: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                } else {
                                    // Editar un comentario existente
                                    selectedComment?.let { comment ->
                                        editComment(
                                            comment = comment,
                                            newCommentText = editCommentText,
                                            db = db,
                                            onSuccess = {
                                                comment.comment = editCommentText
                                                showEditDialog = false
                                                Toast.makeText(
                                                    context,
                                                    "Comentario editado con éxito",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onFailure = { errorMessage ->
                                                Toast.makeText(
                                                    context,
                                                    "Error al editar: $errorMessage",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        )
                                    }
                                }
                            }) {
                                Text(if (selectedComment == null) "Agregar" else "Guardar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Campo para responder si eres admin
                if (isAdmin && selectedComment != null) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Responder comentario") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { addReplyToComment() }) {
                        Text("Enviar Respuesta")
                    }
                }
            }
        }
    )
}

@Composable
fun CommentItem(
    comment: Comment,
    isAdmin: Boolean,
    currentUserId: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReply: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Imagen de perfil o ícono predeterminado
        if (!comment.profileImageBase64.isNullOrEmpty()) {
            val bitmap = decodeBase64ToBitmap(comment.profileImageBase64)
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Imagen de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
            }
        } else {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Imagen de perfil predeterminada",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Columna principal con detalles del comentario
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.username,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "• ${comment.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                if (comment.userId == currentUserId) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar comentario",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onDelete() }) { // Cambiado a confirmDelete
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Eliminar comentario",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }

            // Estrellas de calificación
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Estrella $i",
                        tint = if (i <= comment.rating) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = comment.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (comment.reply.isNotBlank()) {
                Text(
                    text = "Respuesta: ${comment.reply}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (isAdmin) {
                TextButton(onClick = onReply) {
                    Text("Responder", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// Implementación de funciones para editar y eliminar comentarios
fun editComment(
    comment: Comment,
    newCommentText: String,
    db: FirebaseFirestore,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    db.collection("comments").document(comment.id).update("comment", newCommentText)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e.message ?: "Error desconocido") }
}

fun deleteComment(
    comment: Comment,
    db: FirebaseFirestore,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    db.collection("comments").document(comment.id).delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e.message ?: "Error desconocido") }
}

fun getRelativeTimeSpan(date: Date): String {
    val now = Date()
    val diffInMillis = now.time - date.time

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
    val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)

    return when {
        seconds < 60 -> "Hace un momento"
        minutes < 60 -> "Hace $minutes minutos"
        hours < 24 -> "Hace $hours horas"
        days < 7 -> "Hace $days días"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
}
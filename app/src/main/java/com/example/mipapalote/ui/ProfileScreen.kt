package com.example.mipapalote.ui

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageBase64 by remember { mutableStateOf("") }
    var pushNotifications by remember { mutableStateOf(true) }
    var isEditMode by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            profileImageUri = uri
            coroutineScope.launch {
                val base64Image = encodeImageToBase64(context, uri)
                profileImageBase64 = base64Image
                user?.let { currentUser ->
                    db.collection("users").document(currentUser.uid)
                        .update("profileImageBase64", base64Image)
                }
            }
            Toast.makeText(context, "Image selected", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(user) {
        user?.let { currentUser ->
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
                name = document.getString("name") ?: ""
                age = document.getString("age") ?: ""
                gender = document.getString("gender") ?: ""
                profileImageBase64 = document.getString("profileImageBase64") ?: ""
                pushNotifications = document.getBoolean("pushNotifications") ?: true
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Profile Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8BC34A)),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null || profileImageBase64.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = profileImageUri ?: decodeBase64ToBitmap(profileImageBase64)
                        ),
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default profile picture",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (isEditMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(name.ifEmpty { "User" }, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text(email, fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isEditMode) {
                        // Save changes
                        coroutineScope.launch {
                            user?.let { currentUser ->
                                db.collection("users").document(currentUser.uid)
                                    .update(
                                        mapOf(
                                            "name" to name,
                                            "age" to age,
                                            "gender" to gender,
                                            "profileImageBase64" to profileImageBase64
                                        )
                                    )
                            }
                        }
                        isEditMode = false
                    } else {
                        isEditMode = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(if (isEditMode) "Save changes" else "Edit profile")
            }

            if (isEditMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Change profile picture")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Personal Information", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))

            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (isEditMode) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Email: $email", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Age: $age", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Gender: $gender", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Preferences", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))

            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Push notifications", fontSize = 16.sp)
                    }
                    Switch(
                        checked = pushNotifications,
                        onCheckedChange = {
                            pushNotifications = it
                            coroutineScope.launch {
                                user?.let { currentUser ->
                                    db.collection("users").document(currentUser.uid)
                                        .update("pushNotifications", it)
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color.Red)
            }
        }
    }
}

// Helper functions
fun decodeBase64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

suspend fun encodeImageToBase64(context: android.content.Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        val loader = ImageRequest.Builder(context)
            .data(uri)
            .build()
        val result = (context.imageLoader.execute(loader) as SuccessResult).drawable
        val bitmap = result.toBitmap()
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val byteArray = outputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
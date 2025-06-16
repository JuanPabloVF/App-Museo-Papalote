package com.example.mipapalote.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QRScreen(navController: NavController) {
    val context = LocalContext.current
    val scanResult = remember { mutableStateOf("") }
    var cameraPermissionGranted by remember { mutableStateOf(false) }

    // Solicita el permiso de cámara
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            cameraPermissionGranted = isGranted
            if (isGranted) {
                Log.d("QRScreen", "Permiso de cámara concedido")
                scanQR(context, scanResult)
            } else {
                Log.d("QRScreen", "Permiso de cámara denegado")
                Toast.makeText(
                    context,
                    "Se requiere permiso de cámara para escanear códigos QR",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            cameraPermissionGranted = true
            scanQR(context, scanResult)
        }
    }

    // Animación de pulsación
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5E9)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4CAF50), Color(0xFFAED581)),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícono con animación
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(10.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Ícono de escaneo",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Título estilizado
                Text(
                    "Escanea tu código QR",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    "Apunta la cámara hacia el código QR para registrarte",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de escaneo
                Button(
                    onClick = {
                        if (cameraPermissionGranted) {
                            scanQR(context, scanResult)
                        } else {
                            Toast.makeText(context, "Permiso de cámara requerido", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Iniciar escaneo",
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar escaneo", color = Color(0xFF4CAF50))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Mostrar resultado del escaneo
                if (scanResult.value.isNotEmpty()) {
                    Text(
                        text = "Resultado del escaneo:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = scanResult.value,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Escanea un código QR y registra la visita
fun scanQR(context: Context, scanResult: MutableState<String>) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    val scanner = GmsBarcodeScanning.getClient(context)
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    if (userId == null) {
        Toast.makeText(context, "Usuario no autenticado. Inicia sesión para continuar.", Toast.LENGTH_LONG).show()
        return
    }

    scanner.startScan()
        .addOnSuccessListener { barcode ->
            val url = barcode.rawValue
            if (!url.isNullOrEmpty()) {
                scanResult.value = url
                Log.d("QRScreen", "URL detectada: $url")
                Toast.makeText(context, "QR escaneado: $url", Toast.LENGTH_SHORT).show()

                val exhibitionId = extractExhibitionID(url)
                if (exhibitionId != null) {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val docRef = db.collection("Visits").document(date)

                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(docRef)
                        val currentData = snapshot.data ?: mapOf<String, Any>()
                        val currentVisitors = (currentData[exhibitionId] as? Map<*, *>)?.get("visitors") as? List<String> ?: emptyList()
                        val currentCount = (currentData[exhibitionId] as? Map<*, *>)?.get("count") as? Long ?: 0

                        val updatedVisitors = if (userId !in currentVisitors) {
                            currentVisitors + userId
                        } else {
                            currentVisitors
                        }

                        val updatedData = mapOf(
                            exhibitionId to mapOf(
                                "count" to currentCount + 1,
                                "visitors" to updatedVisitors
                            )
                        )

                        transaction.set(docRef, updatedData, SetOptions.merge())
                    }.addOnSuccessListener {
                        Log.d("QRScreen", "Visita registrada correctamente.")
                        try {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(browserIntent)
                        } catch (e: Exception) {
                            Log.e("QRScreen", "Error al abrir URL: ${e.message}", e)
                            Toast.makeText(context, "No se pudo abrir la URL", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Log.e("QRScreen", "Error al registrar la visita: ${e.message}", e)
                    }
                } else {
                    Toast.makeText(context, "El QR no identifica una exhibición válida", Toast.LENGTH_SHORT).show()
                }
            }
        }
        .addOnFailureListener { e ->
            Log.e("QRScreen", "Error al escanear QR: ${e.message}", e)
            Toast.makeText(context, "Error al escanear QR", Toast.LENGTH_LONG).show()
        }
}

// Extrae el identificador de la exhibición desde la URL
fun extractExhibitionID(url: String): String? {
    return if (url.contains("?exh=")) {
        url.substringAfter("?exh=").substringBefore("&")
    } else {
        null
    }
}
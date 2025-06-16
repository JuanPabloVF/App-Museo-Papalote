package com.example.mipapalote.ui.admin

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatistics() {
    val db = FirebaseFirestore.getInstance()
    var dailyData by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var weeklyData by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    // Cargar estadísticas
    LaunchedEffect(Unit) {
        loadDailyStatistics(db) { data ->
            dailyData = data
        }
        loadWeeklyStatistics(db) { data ->
            weeklyData = data
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Statistics") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Estadísticas de Visitantes", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Gráfico diario
            Text("Visitantes por exhibición (Hoy)", style = MaterialTheme.typography.bodyLarge)
            BarChartView(data = dailyData, label = "Visitantes Hoy")

            Spacer(modifier = Modifier.height(32.dp))

            // Gráfico semanal
            Text("Visitantes por exhibición (Última semana)", style = MaterialTheme.typography.bodyLarge)
            BarChartView(data = weeklyData, label = "Visitantes Semana")
        }
    }
}

@Composable
fun BarChartView(data: Map<String, Long>, label: String) {
    if (data.isEmpty()) {
        Text("No hay datos disponibles", style = MaterialTheme.typography.bodyMedium)
        return
    }

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = true
            }
        },
        update = { barChart ->
            val entries = data.entries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.toFloat())
            }
            val dataSet = BarDataSet(entries, label).apply {
                colors = ColorTemplate.COLORFUL_COLORS.toList() // Colores únicos por exhibición
            }
            val barData = BarData(dataSet)
            barData.barWidth = 0.9f
            barChart.data = barData
            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(data.keys.toList())
            barChart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}


// Cargar estadísticas diarias
fun loadDailyStatistics(
    db: FirebaseFirestore,
    onDataLoaded: (Map<String, Long>) -> Unit
) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    db.collection("Visits").document(today).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val data = document.data?.mapValues { (_, value) ->
                    (value as? Map<*, *>)?.get("count") as? Long ?: 0L
                } ?: emptyMap()
                onDataLoaded(data)
            } else {
                onDataLoaded(emptyMap())
            }
        }
        .addOnFailureListener { e ->
            Log.e("AdminStatistics", "Error al cargar estadísticas diarias: ${e.message}", e)
            onDataLoaded(emptyMap())
        }
}

/// Cargar estadísticas semanales
fun loadWeeklyStatistics(
    db: FirebaseFirestore,
    onDataLoaded: (Map<String, Long>) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) // Ir al lunes de la semana actual

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val weeklyData = mutableMapOf<String, Long>() // Almacenar el acumulado semanal

    val daysOfWeek = mutableListOf<String>()
    for (i in 0..6) { // Generar fechas de lunes a domingo
        daysOfWeek.add(sdf.format(calendar.time))
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    // Consultar los documentos correspondientes a los días de la semana
    val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()
    for (date in daysOfWeek) {
        val task = db.collection("Visits").document(date).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data ?: emptyMap<String, Any>()
                    for ((key, value) in data) {
                        val count = (value as? Map<*, *>)?.get("count") as? Long ?: 0L
                        weeklyData[key] = (weeklyData[key] ?: 0L) + count
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AdminStatistics", "Error al cargar datos del día $date: ${e.message}", e)
            }
        tasks.add(task)
    }

    // Esperar a que todas las tareas se completen
    com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
        .addOnCompleteListener {
            onDataLoaded(weeklyData)
        }
}

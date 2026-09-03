package com.example.ui.clock

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(onBack: () -> Unit) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var isStopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    LaunchedEffect(isStopwatchRunning) {
        while (isStopwatchRunning) {
            delay(10)
            stopwatchTime += 10
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clock") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Current Time
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            Text(
                text = timeFormat.format(Date(currentTime)),
                style = MaterialTheme.typography.displayLarge,
                fontSize = 64.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Stopwatch
            Text("Stopwatch", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            val minutes = (stopwatchTime / 60000).toString().padStart(2, '0')
            val seconds = ((stopwatchTime % 60000) / 1000).toString().padStart(2, '0')
            val milliseconds = ((stopwatchTime % 1000) / 10).toString().padStart(2, '0')
            
            Text(
                text = "$minutes:$seconds.$milliseconds",
                style = MaterialTheme.typography.displayMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { isStopwatchRunning = !isStopwatchRunning }) {
                    Text(if (isStopwatchRunning) "Pause" else "Start")
                }
                OutlinedButton(onClick = { 
                    isStopwatchRunning = false
                    stopwatchTime = 0L 
                }) {
                    Text("Reset")
                }
            }
        }
    }
}

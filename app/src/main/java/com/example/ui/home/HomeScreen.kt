package com.example.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(onAppClick: (String) -> Unit, settingsRepository: SettingsRepository) {
    val context = LocalContext.current
    val wallpaperUri by settingsRepository.wallpaperUri.collectAsState(initial = null)
    val isDarkModePref by settingsRepository.isDarkMode.collectAsState(initial = null)
    
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var batteryLevel by remember { mutableStateOf(getBatteryLevel(context)) }
    var showControlCenter by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) {
                        batteryLevel = (level * 100 / scale.toFloat()).toInt()
                    }
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount > 20) {
                        // Swipe down
                        showControlCenter = true
                    } else if (dragAmount < -20) {
                        // Swipe up
                        showControlCenter = false
                    }
                }
            }
    ) {
        // Background Wallpaper
        if (wallpaperUri != null) {
            AsyncImage(
                model = wallpaperUri,
                contentDescription = "Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF2B2E33), Color(0xFF131517))
                        )
                    ) // Default dark premium gradient
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status bar mock (Battery)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "$batteryLevel%",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Clock Widget
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            
            Text(
                text = timeFormat.format(Date(currentTime)),
                fontSize = 88.sp,
                color = Color.White,
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = dateFormat.format(Date(currentTime)),
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.weight(1f))

            // App Grid
            val allApps = listOf(
                AppItem("Gallery", Icons.Filled.Image, "gallery", listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))),
                AppItem("Music", Icons.Filled.MusicNote, "music", listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))),
                AppItem("Notes", Icons.Filled.Notes, "notes", listOf(Color(0xFFF2C94C), Color(0xFFF2994A))),
                AppItem("Calculator", Icons.Filled.Calculate, "calculator", listOf(Color(0xFFFF8C00), Color(0xFFFFA500))),
                AppItem("Code", Icons.Filled.Code, "code", listOf(Color(0xFF2B32B2), Color(0xFF1488CC)))
            )

            val dockApps = listOf(
                AppItem("Camera", Icons.Filled.CameraAlt, "camera", listOf(Color(0xFF3A3845), Color(0xFF1C1A27))),
                AppItem("Recorder", Icons.Filled.Mic, "recorder", listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))),
                AppItem("Clock", Icons.Filled.Schedule, "clock", listOf(Color(0xFF4CA1AF), Color(0xFFC4E0E5))),
                AppItem("Settings", Icons.Filled.Settings, "settings", listOf(Color(0xFF636FA4), Color(0xFFE8CBC0)))
            )
            
            // Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                allApps.forEach { app ->
                    AppIcon(app = app, onClick = { onAppClick(app.route) })
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(vertical = 16.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dockApps.forEach { app ->
                        AppIcon(app = app, onClick = { onAppClick(app.route) }, isDock = true)
                    }
                }
            }
        }
        
        // Control Center Overlay
        AnimatedVisibility(
            visible = showControlCenter,
            enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f) // Take up top half of screen
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xFF1A1A1A).copy(alpha = 0.95f))
                    .clickable(enabled = false) {} // Consume clicks so they don't pass through
                    .padding(24.dp)
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Control Center",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showControlCenter = false }) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Toggles Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        var wifiEnabled by remember { mutableStateOf(true) }
                        var bluetoothEnabled by remember { mutableStateOf(false) }
                        var flashlightEnabled by remember { mutableStateOf(false) }
                        
                        val systemDark = isSystemInDarkTheme()
                        val isDark = isDarkModePref ?: systemDark

                        ControlToggle(
                            icon = Icons.Filled.Wifi,
                            label = "Wi-Fi",
                            enabled = wifiEnabled,
                            onToggle = { wifiEnabled = !wifiEnabled }
                        )
                        ControlToggle(
                            icon = Icons.Filled.Bluetooth,
                            label = "Bluetooth",
                            enabled = bluetoothEnabled,
                            onToggle = { bluetoothEnabled = !bluetoothEnabled }
                        )
                        ControlToggle(
                            icon = Icons.Filled.FlashlightOn,
                            label = "Flashlight",
                            enabled = flashlightEnabled,
                            onToggle = { flashlightEnabled = !flashlightEnabled }
                        )
                        ControlToggle(
                            icon = Icons.Filled.DarkMode,
                            label = "Dark Mode",
                            enabled = isDark,
                            onToggle = { 
                                coroutineScope.launch { 
                                    settingsRepository.saveDarkMode(!isDark) 
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Brightness slider mock
                    var brightness by remember { mutableStateOf(0.7f) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.BrightnessLow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(16.dp))
                        Slider(
                            value = brightness,
                            onValueChange = { brightness = it },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(Icons.Filled.BrightnessHigh, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ControlToggle(icon: ImageVector, label: String, enabled: Boolean, onToggle: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (enabled) MaterialTheme.colorScheme.primary else Color.DarkGray)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) MaterialTheme.colorScheme.onPrimary else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AppIcon(app: AppItem, onClick: () -> Unit, isDock: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isDock) 64.dp else 68.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(app.colors)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = app.icon,
                contentDescription = app.name,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        if (!isDock) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

data class AppItem(val name: String, val icon: ImageVector, val route: String, val colors: List<Color>)

private fun getBatteryLevel(context: Context): Int {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}

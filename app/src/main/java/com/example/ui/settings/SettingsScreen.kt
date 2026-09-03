package com.example.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.SettingsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsRepository: SettingsRepository, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val isDarkMode by settingsRepository.isDarkMode.collectAsState(initial = false)
    val wallpaperUri by settingsRepository.wallpaperUri.collectAsState(initial = null)
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                settingsRepository.saveWallpaperUri(it.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text("Display", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
            )
            
            ListItem(
                headlineContent = { Text("Dark Mode") },
                trailingContent = {
                    Switch(
                        checked = isDarkMode ?: false,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                settingsRepository.saveDarkMode(checked)
                            }
                        }
                    )
                }
            )
            
            HorizontalDivider()
            
            ListItem(
                headlineContent = { Text("Wallpaper", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
            )
            
            ListItem(
                headlineContent = { Text("Choose Wallpaper from Gallery") },
                supportingContent = { Text("Select a photo to set as your home screen background") },
                modifier = Modifier.clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            
            ListItem(
                headlineContent = { Text("Reset to Default Wallpaper") },
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        settingsRepository.saveWallpaperUri("") // Reset to default
                    }
                }
            )
        }
    }
}

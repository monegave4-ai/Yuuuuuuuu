package com.example.ui.music

import android.Manifest
import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

data class AudioItem(val id: Long, val title: String, val artist: String, val uri: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MusicScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionState = rememberPermissionState(permission)
    
    var audioList by remember { mutableStateOf<List<AudioItem>>(emptyList()) }
    var currentPlaying by remember { mutableStateOf<AudioItem?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })
        }
    }
    
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val query = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
            )
            
            query?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                
                val audios = mutableListOf<AudioItem>()
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    audios.add(
                        AudioItem(
                            id = id,
                            title = cursor.getString(titleCol),
                            artist = cursor.getString(artistCol),
                            uri = uri.toString()
                        )
                    )
                }
                audioList = audios
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Music") },
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
            if (!permissionState.status.isGranted) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Grant Storage Permission")
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(audioList) { audio ->
                        ListItem(
                            headlineContent = { Text(audio.title, maxLines = 1) },
                            supportingContent = { Text(audio.artist, maxLines = 1) },
                            leadingContent = {
                                Icon(Icons.Filled.MusicNote, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                currentPlaying = audio
                                exoPlayer.setMediaItem(MediaItem.fromUri(audio.uri))
                                exoPlayer.prepare()
                                exoPlayer.play()
                            }
                        )
                        HorizontalDivider()
                    }
                }
                
                // Player Controls
                currentPlaying?.let { playing ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(playing.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                Text(playing.artist, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            }
                            Row {
                                IconButton(onClick = { 
                                    exoPlayer.seekToPreviousMediaItem() // Basic implementation
                                }) {
                                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                                }
                                IconButton(onClick = { 
                                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                }) {
                                    Icon(
                                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = "Play/Pause"
                                    )
                                }
                                IconButton(onClick = { 
                                    exoPlayer.seekToNextMediaItem() // Basic implementation
                                }) {
                                    Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

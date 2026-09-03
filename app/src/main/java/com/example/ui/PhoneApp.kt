package com.example.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.PhoneApplication
import com.example.ui.home.HomeScreen
import com.example.ui.gallery.GalleryScreen
import com.example.ui.music.MusicScreen
import com.example.ui.notes.NotesScreen
import com.example.ui.camera.CameraScreen
import com.example.ui.recorder.RecorderScreen
import com.example.ui.clock.ClockScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.calculator.CalculatorScreen
import com.example.ui.codeeditor.CodeEditorScreen

@Composable
fun PhoneApp(application: PhoneApplication) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize(),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable("home") {
            HomeScreen(
                onAppClick = { appRoute -> navController.navigate(appRoute) },
                settingsRepository = application.container.settingsRepository
            )
        }
        composable("gallery") { GalleryScreen { navController.popBackStack() } }
        composable("music") { MusicScreen { navController.popBackStack() } }
        composable("notes") { NotesScreen(application.container.noteRepository) { navController.popBackStack() } }
        composable("camera") { CameraScreen { navController.popBackStack() } }
        composable("recorder") { RecorderScreen { navController.popBackStack() } }
        composable("clock") { ClockScreen { navController.popBackStack() } }
        composable("settings") { 
            SettingsScreen(application.container.settingsRepository) { navController.popBackStack() } 
        }
        composable("calculator") { CalculatorScreen { navController.popBackStack() } }
        composable("code") { CodeEditorScreen { navController.popBackStack() } }
    }
}

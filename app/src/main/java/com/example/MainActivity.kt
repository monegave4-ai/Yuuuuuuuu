package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.PhoneApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val app = application as PhoneApplication
    
    setContent {
      val isDarkModePref by app.container.settingsRepository.isDarkMode.collectAsState(initial = null)
      val useDarkTheme = isDarkModePref ?: isSystemInDarkTheme()
      
      MyApplicationTheme(darkTheme = useDarkTheme) {
        PhoneApp(application = app)
      }
    }
  }
}

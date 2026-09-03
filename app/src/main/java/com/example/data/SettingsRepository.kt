package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    
    companion object {
        val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    val wallpaperUri: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[WALLPAPER_URI]
        }

    val isDarkMode: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE]
        }

    suspend fun saveWallpaperUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[WALLPAPER_URI] = uri
        }
    }

    suspend fun saveDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDarkMode
        }
    }
}

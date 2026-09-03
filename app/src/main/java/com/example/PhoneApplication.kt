package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.NoteRepository
import com.example.data.SettingsRepository

class AppContainer(private val applicationContext: Context) {
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "phone_database"
        ).build()
    }

    val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }
}

class PhoneApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

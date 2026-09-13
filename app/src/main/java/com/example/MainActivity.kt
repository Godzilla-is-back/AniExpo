package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.local.AppDatabase
import com.example.data.repository.AnimeRepository
import com.example.ui.SakuraMainApp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = AnimeRepository(database.animeDao())

    setContent {
      SakuraMainApp(repository = repository)
    }
  }
}


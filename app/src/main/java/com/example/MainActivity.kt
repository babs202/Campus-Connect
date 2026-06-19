package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.CampusConnectDatabase
import com.example.data.CampusConnectRepository
import com.example.ui.screens.CampusConnectApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CampusConnectViewModel

class MainActivity : ComponentActivity() {
    private lateinit var database: CampusConnectDatabase
    private lateinit var repository: CampusConnectRepository
    private lateinit var viewModel: CampusConnectViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SQLite Offline Persistence Room DB
        database = Room.databaseBuilder(
            applicationContext,
            CampusConnectDatabase::class.java,
            "campus_connect_usted.db"
        )
        .fallbackToDestructiveMigration()
        .build()

        repository = CampusConnectRepository(database.dao)

        // Instantiate ViewModel with manual Provider Factory to prevent DI complexity
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CampusConnectViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return CampusConnectViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(this, factory)[CampusConnectViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CampusConnectApp(viewModel = viewModel)
                }
            }
        }
    }
}


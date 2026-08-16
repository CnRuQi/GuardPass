package com.example.myandroid.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.myandroid.ui.add.AddEditActivity
import com.example.myandroid.ui.category.CategoryActivity
import com.example.myandroid.ui.search.SearchActivity
import com.example.myandroid.ui.settings.SettingsActivity
import com.example.myandroid.ui.theme.VisionOSPasswordManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisionOSPasswordManagerTheme {
                MainScreen(
                    onNavigateToAdd = {
                        startActivity(Intent(this, AddEditActivity::class.java))
                    },
                    onNavigateToSearch = {
                        startActivity(Intent(this, SearchActivity::class.java))
                    },
                    onNavigateToCategory = {
                        startActivity(Intent(this, CategoryActivity::class.java))
                    },
                    onNavigateToSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onNavigateToEdit = { id ->
                        val intent = Intent(this, AddEditActivity::class.java)
                        intent.putExtra("password_id", id)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

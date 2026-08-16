package com.example.myandroid.ui.search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myandroid.ui.theme.VisionOSPasswordManagerTheme

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisionOSPasswordManagerTheme {
                SearchScreen(onBack = { finish() })
            }
        }
    }
}

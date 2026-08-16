package com.example.myandroid.ui.category

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myandroid.ui.theme.VisionOSPasswordManagerTheme

class CategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisionOSPasswordManagerTheme {
                CategoryScreen(onBack = { finish() })
            }
        }
    }
}

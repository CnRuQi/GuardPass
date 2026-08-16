package com.example.myandroid.ui.add

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myandroid.ui.theme.VisionOSPasswordManagerTheme

class AddEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val passwordId = intent.getLongExtra("password_id", -1)
        setContent {
            VisionOSPasswordManagerTheme {
                AddEditScreen(
                    passwordId = passwordId,
                    onBack = { finish() }
                )
            }
        }
    }
}

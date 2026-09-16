package com.example.myandroid.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.myandroid.R
import com.example.myandroid.ui.AppNavHost
import com.example.myandroid.ui.theme.VisionOSPasswordManagerTheme
import com.example.myandroid.util.BiometricHelper

class MainActivity : FragmentActivity() {
    private val locked = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        locked.value = shouldLock()
        setContent {
            VisionOSPasswordManagerTheme {
                AppLockGate(
                    activity = this,
                    locked = locked.value,
                    onUnlock = { locked.value = false }
                ) {
                    AppNavHost()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations && shouldLock()) {
            locked.value = true
        }
    }

    private fun shouldLock(): Boolean =
        BiometricHelper.isEnabled(this) && BiometricHelper.isBiometricAvailable(this)
}

@Composable
private fun AppLockGate(
    activity: FragmentActivity,
    locked: Boolean,
    onUnlock: () -> Unit,
    content: @Composable () -> Unit
) {
    var attempt by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    if (!locked) {
        content()
    } else {
        LaunchedEffect(locked, attempt) {
            BiometricHelper.authenticate(activity, object : BiometricHelper.AuthenticationCallback {
                override fun onAuthenticated() {
                    error = null
                    onUnlock()
                }

                override fun onError(message: String) {
                    error = message.ifBlank { "验证未完成" }
                }

                override fun onFailed() {
                    error = "未匹配，请重试"
                }
            })
        }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lock),
                        contentDescription = "应用已锁定",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("GuardPass 已锁定", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        error ?: "验证身份后继续",
                        color = if (error == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { error = null; attempt++ }) {
                        Text("重新验证")
                    }
                }
            }
        }
    }
}

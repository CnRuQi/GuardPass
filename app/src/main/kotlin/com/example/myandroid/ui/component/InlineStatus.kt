package com.example.myandroid.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class StatusTone { Neutral, Success, Warning, Error }

@Composable
fun InlineStatus(
    message: String,
    tone: StatusTone = StatusTone.Neutral,
    modifier: Modifier = Modifier
) {
    val color = when (tone) {
        StatusTone.Success -> MaterialTheme.colorScheme.primary
        StatusTone.Warning -> MaterialTheme.colorScheme.tertiary
        StatusTone.Error -> MaterialTheme.colorScheme.error
        StatusTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Spacer(Modifier.width(4.dp))
        Text(message, color = color, style = MaterialTheme.typography.bodyMedium)
    }
}

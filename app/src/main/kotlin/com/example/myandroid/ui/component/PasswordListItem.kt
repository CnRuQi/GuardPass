package com.example.myandroid.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myandroid.R
import com.example.myandroid.data.db.entity.PasswordEntry

@Composable
fun PasswordListItem(
    entry: PasswordEntry,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopyUsername: () -> Unit,
    onCopySecret: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secretVisible by rememberSaveable(entry.id) { mutableStateOf(false) }
    val secret = entry.password.orEmpty()
    val maskedSecret = "••••••••••••"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpen),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 14.dp, end = 8.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                if (!entry.isApiKey && !entry.username.isNullOrBlank()) {
                    SecretLine(
                        value = entry.username.orEmpty(),
                        masked = false,
                        contentDescription = "复制账号",
                        onCopy = onCopyUsername
                    )
                    Spacer(Modifier.height(2.dp))
                }
                SecretLine(
                    value = if (secretVisible) secret else maskedSecret,
                    masked = !secretVisible,
                    contentDescription = if (secretVisible) "隐藏密钥" else "显示密钥",
                    onCopy = { if (secret.isNotEmpty()) onCopySecret() },
                    onToggleVisibility = { secretVisible = !secretVisible }
                )
                if (!entry.url.isNullOrBlank()) {
                    Text(
                        entry.url.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    painter = painterResource(if (entry.isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline),
                    contentDescription = if (entry.isFavorite) "取消收藏" else "收藏",
                    tint = if (entry.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SecretLine(
    value: String,
    masked: Boolean,
    contentDescription: String,
    onCopy: () -> Unit,
    onToggleVisibility: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (masked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            fontFamily = if (masked) FontFamily.Monospace else FontFamily.Default,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (onToggleVisibility != null) {
            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(32.dp)) {
                Icon(
                    painter = painterResource(if (masked) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                    contentDescription = contentDescription,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_copy),
                contentDescription = "复制",
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

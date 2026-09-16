package com.example.myandroid.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myandroid.ui.backup.BackupViewModel
import com.example.myandroid.ui.component.AppSectionHeader
import com.example.myandroid.ui.component.InlineStatus
import com.example.myandroid.ui.component.StatusTone
import com.example.myandroid.util.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenV2(
    contentPadding: PaddingValues,
    settingsViewModel: SettingsViewModel = viewModel(),
    backupViewModel: BackupViewModel = viewModel()
) {
    val context = LocalContext.current
    val config by settingsViewModel.config.observeAsState(SettingsViewModel.WebDavConfig())
    val connectionStatus by settingsViewModel.connectionStatus.observeAsState()
    val syncStatus by settingsViewModel.syncStatus.observeAsState()
    val backupState by backupViewModel.state.collectAsStateWithLifecycle()
    val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }
    var biometricEnabled by remember { mutableStateOf(BiometricHelper.isEnabled(context)) }
    var serverUrl by remember(config.serverUrl) { mutableStateOf(config.serverUrl) }
    var username by remember(config.username) { mutableStateOf(config.username) }
    var password by remember(config.password) { mutableStateOf(config.password) }
    var showBackupPrompt by remember { mutableStateOf<String?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreContent by remember { mutableStateOf<String?>(null) }
    val syncBusy = syncStatus?.key == "uploading" || syncStatus?.key == "downloading"

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val content = backupState.exportContent
        if (uri != null && content != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
            backupViewModel.clearMessage()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            pendingRestoreContent = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            if (!pendingRestoreContent.isNullOrBlank()) showRestoreConfirm = true
        }
    }

    LaunchedEffect(backupState.exportContent) {
        if (backupState.exportContent != null) exportLauncher.launch("guardpass_backup.enc")
    }

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        TopAppBar(
            title = { Text("设置") },
            windowInsets = WindowInsets(0, 0, 0, 0)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        item {
            AppSectionHeader("安全", "保护应用访问和本地数据")
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("应用锁", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (biometricAvailable) "使用指纹、面部或设备锁验证" else "设备未提供可用的身份验证",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = {
                            biometricEnabled = it
                            BiometricHelper.setEnabled(context, it)
                        },
                        enabled = biometricAvailable
                    )
                }
            }
        }
        item {
            AppSectionHeader("数据", "加密备份可以保存到本地或 WebDAV")
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showBackupPrompt = "export" },
                        enabled = !backupState.isBusy && !syncBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("导出加密备份") }
                    TextButton(
                        onClick = { importLauncher.launch("application/octet-stream") },
                        enabled = !backupState.isBusy && !syncBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("从文件恢复") }
                    if (backupState.isBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    backupState.message?.let {
                        InlineStatus(it, if (backupState.success) StatusTone.Success else StatusTone.Error)
                    }
                }
            }
        }
        item {
            AppSectionHeader("WebDAV", "配置后可上传和恢复同一种加密备份")
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(serverUrl, { serverUrl = it }, label = { Text("服务器地址") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(username, { username = it }, label = { Text("用户名") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(
                        password,
                        { password = it },
                        label = { Text("密码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Button(
                        onClick = { settingsViewModel.updateConfig(SettingsViewModel.WebDavConfig(serverUrl, username, password)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("保存连接配置") }
                    Button(
                        onClick = { settingsViewModel.testConnection() },
                        enabled = connectionStatus?.key != "connecting",
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("测试连接") }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { showBackupPrompt = "upload" },
                            enabled = !backupState.isBusy && !syncBusy,
                            modifier = Modifier.weight(1f)
                        ) { Text("上传备份") }
                        TextButton(
                            onClick = { showBackupPrompt = "download" },
                            enabled = !backupState.isBusy && !syncBusy,
                            modifier = Modifier.weight(1f)
                        ) { Text("恢复备份") }
                    }
                    connectionStatus?.let { InlineStatus(connectionMessage(it), if (it.success) StatusTone.Success else StatusTone.Error) }
                    syncStatus?.let { InlineStatus(syncMessage(it), if (it.success) StatusTone.Success else StatusTone.Error) }
                }
            }
        }
    }
    }

    showBackupPrompt?.takeUnless { it == "import" }?.let { action ->
        PasswordPromptDialog(
            title = when (action) {
                "export" -> "导出备份"
                "upload" -> "上传备份"
                else -> "恢复备份"
            },
            confirmLabel = "继续",
            requiresConfirmation = action == "export" || action == "upload",
            onDismiss = { showBackupPrompt = null },
            onConfirm = { key ->
                showBackupPrompt = null
                when (action) {
                    "export" -> backupViewModel.export(key)
                    "upload" -> settingsViewModel.uploadBackup(key)
                    "download" -> settingsViewModel.downloadAndRestore(key)
                }
            }
        )
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false; pendingRestoreContent = null },
            title = { Text("覆盖本地数据？") },
            text = { Text("恢复会替换当前密码和分类，文件格式与 WebDAV 备份一致。") },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreConfirm = false
                    showBackupPrompt = "import"
                }) { Text("继续") }
            },
            dismissButton = { TextButton(onClick = { showRestoreConfirm = false }) { Text("取消") } }
        )
    }
    if (showBackupPrompt == "import") {
        PasswordPromptDialog(
            title = "恢复本地备份",
            confirmLabel = "恢复",
            requiresConfirmation = false,
            onDismiss = { showBackupPrompt = null },
            onConfirm = { key ->
                showBackupPrompt = null
                pendingRestoreContent?.let { backupViewModel.restore(it, key) }
            }
        )
    }
}

@Composable
private fun PasswordPromptDialog(
    title: String,
    confirmLabel: String,
    requiresConfirmation: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember(title) { mutableStateOf("") }
    var confirmation by remember(title) { mutableStateOf("") }
    var error by remember(title) { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    password,
                    { password = it; error = null },
                    label = { Text("加密密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (requiresConfirmation) {
                    OutlinedTextField(
                        confirmation,
                        { confirmation = it; error = null },
                        label = { Text("确认密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    password.isBlank() -> error = "请输入加密密码"
                    requiresConfirmation && password != confirmation -> error = "两次密码不一致"
                    else -> onConfirm(password)
                }
            }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun connectionMessage(status: SettingsViewModel.StatusMessage): String = when (status.key) {
    "connecting" -> "正在测试连接…"
    "connection_success" -> "连接成功"
    "connection_failed_server" -> "服务器返回错误"
    "config_failed" -> "配置保存失败：${status.param.orEmpty()}"
    else -> "连接失败：${status.param.orEmpty()}"
}

private fun syncMessage(status: SettingsViewModel.StatusMessage): String = when (status.key) {
    "uploading" -> "正在上传…"
    "upload_success" -> "上传成功（${status.param.orEmpty()} 条记录）"
    "downloading" -> "正在恢复…"
    "download_success" -> "恢复成功（${status.param.orEmpty()} 条记录）"
    else -> "操作失败：${status.param.orEmpty()}"
}

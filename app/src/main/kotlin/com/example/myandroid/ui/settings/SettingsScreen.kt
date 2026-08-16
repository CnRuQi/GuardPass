package com.example.myandroid.ui.settings

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myandroid.R
import com.example.myandroid.ui.component.*
import com.example.myandroid.ui.theme.*
import com.example.myandroid.util.BiometricHelper

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val config by viewModel.config.observeAsState()
    val connectionStatus by viewModel.connectionStatus.observeAsState()
    val syncStatus by viewModel.syncStatus.observeAsState()

    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var encryptionKey by remember { mutableStateOf("") }

    // Biometric state
    var biometricEnabled by remember {
        mutableStateOf(BiometricHelper.isEnabled(context))
    }
    val biometricAvailable = remember {
        BiometricHelper.isBiometricAvailable(context)
    }

    // Load config
    LaunchedEffect(config) {
        config?.let {
            serverUrl = it.serverUrl
            username = it.username
            password = it.password
        }
    }

    // Status toasts
    LaunchedEffect(connectionStatus) {
        connectionStatus?.let { status ->
            val color = if (status.success) SuccessGreen else ErrorRed
            val msg = when (status.key) {
                "connecting" -> "正在连接..."
                "connection_success" -> "连接成功"
                "connection_failed_server" -> "连接失败: 服务器返回错误"
                "connection_failed" -> "连接失败: ${status.param}"
                else -> status.key
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(syncStatus) {
        syncStatus?.let { status ->
            val msg = when (status.key) {
                "uploading" -> "正在上传..."
                "upload_success" -> "上传成功 (${status.param} 条记录)"
                "upload_failed" -> "上传失败: ${status.param}"
                "downloading" -> "正在下载..."
                "download_success" -> "恢复成功 (${status.param} 条记录)"
                "download_failed" -> "恢复失败: ${status.param}"
                else -> status.key
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    var showDownloadConfirm by remember { mutableStateOf(false) }

    fun saveConfig() {
        viewModel.updateConfig(
            SettingsViewModel.WebDavConfig(
                serverUrl = serverUrl,
                username = username,
                password = password
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(GradientMid, GradientStart, GradientEnd)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.3f))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .height(52.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .springScaleClickable(scaleDownTo = 0.85f, onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("‹", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Light)
                }
                Text(
                    text = "设置",
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // ── Security Section ──
                SectionHeader("安全设置", "启用生物识别以保护密码安全")
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.45f),
                    borderColor = Color.White.copy(alpha = 0.6f),
                    padding = PaddingValues(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "生物识别锁",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                text = if (biometricAvailable) "启用后需要指纹或面部识别验证"
                                else "设备不支持生物识别",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                letterSpacing = (-0.1).sp
                            )
                        }
                        // Toggle switch
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (biometricEnabled) Purple500 else Color.Gray.copy(alpha = 0.3f),
                                    RoundedCornerShape(16.dp)
                                )
                                .springScaleClickable(
                                    enabled = biometricAvailable,
                                    scaleDownTo = 0.92f
                                ) {
                                    if (biometricAvailable) {
                                        biometricEnabled = !biometricEnabled
                                        BiometricHelper.setEnabled(context, biometricEnabled)
                                        Toast.makeText(context,
                                            if (biometricEnabled) "生物识别已启用" else "生物识别已关闭",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = if (biometricEnabled) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(28.dp)
                                    .background(Color.White, RoundedCornerShape(14.dp))
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── WebDAV Section ──
                SectionHeader("WebDAV 同步", "配置 WebDAV 服务器以自动同步密码数据")

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.45f),
                    borderColor = Color.White.copy(alpha = 0.6f),
                    padding = PaddingValues(16.dp)
                ) {
                    Column {
                        SettingField("服务器地址", "https://example.com/remote.php/dav", serverUrl,
                            { serverUrl = it },
                            keyboardType = KeyboardType.Uri)
                        Spacer(Modifier.height(10.dp))
                        SettingField("用户名", "请输入用户名", username,
                            { username = it })
                        Spacer(Modifier.height(10.dp))
                        SettingField("密码", "请输入密码", password,
                            { password = it },
                            keyboardType = KeyboardType.Password)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Sync Section ──
                SectionHeader("同步操作", "使用加密密码同步数据到 WebDAV 服务器")

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.45f),
                    borderColor = Color.White.copy(alpha = 0.6f),
                    padding = PaddingValues(16.dp)
                ) {
                    Column {
                        SettingField("加密密码", "请输入加密密码", encryptionKey,
                            { encryptionKey = it },
                            keyboardType = KeyboardType.Password)

                        Spacer(Modifier.height(16.dp))

                        GlassButton(
                            text = "测试连接",
                            onClick = {
                                saveConfig()
                                viewModel.testConnection()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassButtonOutlined(
                                text = "上传备份",
                                onClick = {
                                    saveConfig()
                                    if (encryptionKey.isBlank()) {
                                        Toast.makeText(context, "请输入加密密码", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.uploadBackup(encryptionKey)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            GlassButtonOutlined(
                                text = "下载恢复",
                                onClick = {
                                    if (encryptionKey.isBlank()) {
                                        Toast.makeText(context, "请输入加密密码", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showDownloadConfirm = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }

    // Download confirmation
    GlassDialog(
        visible = showDownloadConfirm,
        onDismiss = { showDownloadConfirm = false },
        title = "确认恢复",
        message = "从服务器下载数据将覆盖本地所有密码记录，确定继续吗？",
        confirmText = "确定",
        cancelText = "取消",
        onConfirm = {
            saveConfig()
            viewModel.downloadAndRestore(encryptionKey)
        }
    )
}

@Composable
private fun SectionHeader(title: String, description: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = description,
        color = TextSecondary,
        fontSize = 13.sp,
        letterSpacing = (-0.1).sp
    )
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SettingField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        GlassTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            keyboardType = keyboardType,
            imeAction = ImeAction.Next,
            height = 48.dp
        )
    }
}

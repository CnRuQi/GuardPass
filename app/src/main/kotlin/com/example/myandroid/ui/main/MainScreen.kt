package com.example.myandroid.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myandroid.R
import com.example.myandroid.data.db.AppDatabase
import com.example.myandroid.data.db.entity.PasswordEntry
import com.example.myandroid.data.model.ExportData
import com.example.myandroid.data.repository.CategoryRepository
import com.example.myandroid.data.repository.PasswordRepository
import com.example.myandroid.ui.component.*
import com.example.myandroid.ui.theme.*
import com.example.myandroid.util.CryptoUtils
import com.google.gson.Gson

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateToAdd: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCategory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.allCategories.observeAsState(emptyList())
    val passwords by viewModel.displayedPasswords.observeAsState(emptyList())

    var showMenu by remember { mutableStateOf(false) }

    // ── Export / Import State ──
    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<ExportImportAction?>(null) }
    var exportImportPassword by remember { mutableStateOf("") }

    val createFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let { doExport(context, exportImportPassword, uri) } }

    val openFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { doImport(context, exportImportPassword, uri) } }

    fun handleExport() {
        showMenu = false
        pendingAction = ExportImportAction.EXPORT
        showPasswordDialog = true
    }

    fun handleImport() {
        showMenu = false
        pendingAction = ExportImportAction.IMPORT
        showPasswordDialog = true
    }

    // ── Main UI ──
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
            // ── Floating Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.3f))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "密码",
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .springScaleClickable(scaleDownTo = 0.85f) { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = "更多",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ── Glass Search Bar ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(48.dp)
                    .clip(TextFieldShape)
                    .background(Color.White.copy(alpha = 0.4f), TextFieldShape)
                    .border(0.5.dp, Color.White.copy(alpha = 0.5f), TextFieldShape)
                    .springScaleClickable(scaleDownTo = 0.97f) { onNavigateToSearch() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "搜索主题或账号...",
                        color = TextSecondary,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp
                    )
                }
            }

            // ── Category Chips ──
            LazyRowCategoryChips(
                categories = categories,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            Spacer(Modifier.height(4.dp))

            // ── Password List or Empty State (Animated) ──
            AnimatedContent(
                targetState = passwords.isEmpty(),
                transitionSpec = {
                    fadeIn(animationSpec = SpringBouncy) togetherWith
                        fadeOut(animationSpec = SpringBouncy)
                },
                label = "list_empty"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyState(modifier = Modifier.fillMaxSize())
                } else {
                    PasswordList(
                        passwords = passwords,
                        onItemClick = { onNavigateToEdit(it.id) },
                        onCopyUsername = { entry ->
                            copyToClipboard(context, "账号", entry.username)
                        },
                        onCopyPassword = { entry ->
                            copyToClipboard(context, "密码", entry.password)
                        },
                        onToggleFavorite = { entry -> viewModel.toggleFavorite(entry) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // ── FAB ──
        GlassIconButton(
            onClick = onNavigateToAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .navigationBarsPadding(),
            size = 60.dp,
            backgroundColor = Purple500
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = "添加",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // ── Overflow Menu ──
    if (showMenu) {
        GlassMenuSheet(
            onDismiss = { showMenu = false },
            onCategory = { showMenu = false; onNavigateToCategory() },
            onExport = { handleExport() },
            onImport = { handleImport() },
            onSettings = { showMenu = false; onNavigateToSettings() }
        )
    }

    // ── Export / Import Password Dialog ──
    if (showPasswordDialog) {
        ExportImportPasswordDialog(
            isImport = pendingAction == ExportImportAction.IMPORT,
            onDismiss = {
                showPasswordDialog = false
                pendingAction = null
            },
            onConfirm = { password ->
                exportImportPassword = password
                showPasswordDialog = false
                when (pendingAction) {
                    ExportImportAction.EXPORT -> createFileLauncher.launch("password_backup.enc")
                    ExportImportAction.IMPORT -> openFileLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                    null -> {}
                }
                pendingAction = null
            }
        )
    }
}

// ── Export/Import Action Enum ──
private enum class ExportImportAction { EXPORT, IMPORT }

// ── Export/Import Implementation ──
private fun doExport(context: Context, password: String, uri: android.net.Uri) {
    AppDatabase.databaseWriteExecutor.execute {
        try {
            val pwRepo = PasswordRepository(context.applicationContext as android.app.Application)
            val catRepo = CategoryRepository(context.applicationContext as android.app.Application)
            val passwords = pwRepo.allPasswordsSync
            val categories = catRepo.allCategoriesSync
            val data = ExportData()
            data.version = 1
            data.timestamp = System.currentTimeMillis()
            data.categories = categories
            data.passwords = passwords
            val json = Gson().toJson(data)
            val encrypted = CryptoUtils.encrypt(json, password)
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(encrypted.toByteArray(Charsets.UTF_8))
            }
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "数据导出成功", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

private fun doImport(context: Context, password: String, uri: android.net.Uri) {
    AppDatabase.databaseWriteExecutor.execute {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, "无法读取文件", Toast.LENGTH_SHORT).show()
                }
                return@execute
            }
            val encrypted = inputStream.bufferedReader(Charsets.UTF_8).readText()
            inputStream.close()
            val decrypted = CryptoUtils.decrypt(encrypted, password)
            val type = object : com.google.gson.reflect.TypeToken<ExportData>() {}.type
            val data: ExportData = Gson().fromJson(decrypted, type)
            if (data.passwords == null) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, "导入失败: 数据格式错误", Toast.LENGTH_SHORT).show()
                }
                return@execute
            }
            val pwRepo = PasswordRepository(context.applicationContext as android.app.Application)
            pwRepo.deleteAll()
            if (data.passwords.isNotEmpty()) pwRepo.insertAll(data.passwords)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "数据导入成功 (${data.passwords.size} 条)", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// ── Category Chips ──
@Composable
private fun LazyRowCategoryChips(
    categories: List<com.example.myandroid.data.db.entity.Category>,
    onCategorySelected: (Long) -> Unit
) {
    var selectedId by remember { mutableStateOf(MainViewModel.CATEGORY_ALL) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            GlassChip(
                text = "全部",
                selected = selectedId == MainViewModel.CATEGORY_ALL,
                onClick = {
                    selectedId = MainViewModel.CATEGORY_ALL
                    onCategorySelected(MainViewModel.CATEGORY_ALL)
                }
            )
        }
        item {
            GlassChip(
                text = "收藏",
                selected = selectedId == MainViewModel.CATEGORY_FAVORITES,
                onClick = {
                    selectedId = MainViewModel.CATEGORY_FAVORITES
                    onCategorySelected(MainViewModel.CATEGORY_FAVORITES)
                }
            )
        }
        items(categories) { category ->
            GlassChip(
                text = category.name,
                selected = selectedId == category.id,
                onClick = {
                    selectedId = category.id
                    onCategorySelected(category.id)
                }
            )
        }
    }
}

// ── Password List (with staggered item animation) ──
@Composable
private fun PasswordList(
    passwords: List<PasswordEntry>,
    onItemClick: (PasswordEntry) -> Unit,
    onCopyUsername: (PasswordEntry) -> Unit,
    onCopyPassword: (PasswordEntry) -> Unit,
    onToggleFavorite: (PasswordEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(passwords, key = { _, entry -> entry.id }) { index, entry ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(SpringBouncy) + slideInVertically(
                    initialOffsetY = { it / 8 }
                )
            ) {
                PasswordCard(
                    entry = entry,
                    onClick = { onItemClick(entry) },
                    onCopyUsername = { onCopyUsername(entry) },
                    onCopyPassword = { onCopyPassword(entry) },
                    onToggleFavorite = { onToggleFavorite(entry) }
                )
            }
        }
    }
}

// ── Password Card ──
@Composable
private fun PasswordCard(
    entry: PasswordEntry,
    onClick: () -> Unit,
    onCopyUsername: () -> Unit,
    onCopyPassword: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.45f),
        borderColor = Color.White.copy(alpha = 0.6f),
        borderWidth = 0.5.dp,
        padding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Purple500.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    tint = Purple500,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                // Username row with copy button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.username,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .springScaleClickable(scaleDownTo = 0.8f, onClick = onCopyUsername),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_copy),
                            contentDescription = "复制账号",
                            tint = TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                if (!entry.isApiKey) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (passwordVisible) entry.password else "●●●●●●●●",
                            color = if (passwordVisible) TextPrimary else PasswordMask,
                            fontSize = 14.sp,
                            fontFamily = if (passwordVisible) androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .springScaleClickable(scaleDownTo = 0.8f) { passwordVisible = !passwordVisible },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible) R.drawable.ic_visibility_off
                                    else R.drawable.ic_visibility
                                ),
                                contentDescription = "切换密码",
                                tint = TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .springScaleClickable(scaleDownTo = 0.8f, onClick = onCopyPassword),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_copy),
                                contentDescription = "复制密码",
                                tint = TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .springScaleClickable(scaleDownTo = 0.75f, onClick = onToggleFavorite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (entry.isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                    ),
                    contentDescription = "收藏",
                    tint = if (entry.isFavorite) WarningOrange else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Empty State ──
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.ic_empty_box),
                contentDescription = null,
                tint = TextTertiary.copy(alpha = 0.4f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "暂无密码记录",
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Menu Sheet ──
@Composable
private fun GlassMenuSheet(
    onDismiss: () -> Unit,
    onCategory: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onSettings: () -> Unit
) {
    GlassDialog(
        visible = true,
        onDismiss = onDismiss,
        title = "更多操作",
        confirmText = "关闭",
        cancelText = null,
        onConfirm = onDismiss,
        content = {
            Column {
                MenuRow("分类管理", R.drawable.ic_category, onCategory)
                MenuRow("导出数据", R.drawable.ic_upload, onExport)
                MenuRow("导入数据", R.drawable.ic_download, onImport)
                MenuRow("设置", R.drawable.ic_settings, onSettings)
            }
        }
    )
}

@Composable
private fun MenuRow(text: String, iconRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .springScaleClickable(scaleDownTo = 0.96f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Purple500,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 16.sp,
            letterSpacing = (-0.2).sp
        )
    }
}

// ── Export/Import Password Dialog (standalone, doesn't use GlassDialog for error handling) ──
@Composable
private fun ExportImportPasswordDialog(
    isImport: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .clip(DialogShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.7f))
                        ),
                        DialogShape
                    )
                    .border(0.5.dp, Color.White.copy(alpha = 0.5f), DialogShape)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null, onClick = {}
                    )
                    .padding(24.dp)
            ) {
                Text(
                    text = if (isImport) "导入数据" else "导出数据",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                Spacer(Modifier.height(16.dp))
                GlassTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    placeholder = "请输入加密密码",
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                )
                Spacer(Modifier.height(8.dp))
                GlassTextField(
                    value = confirm,
                    onValueChange = { confirm = it; error = null },
                    placeholder = "请确认加密密码",
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                )
                if (error != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(error!!, color = ErrorRed, fontSize = 12.sp)
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    GlassTextButton(text = "取消", onClick = onDismiss, textColor = TextPrimary)
                    Spacer(Modifier.width(8.dp))
                    GlassTextButton(
                        text = "确定",
                        onClick = {
                            when {
                                password.isBlank() -> error = "请输入加密密码"
                                password != confirm -> error = "两次密码不一致"
                                else -> onConfirm(password)
                            }
                        },
                        textColor = Purple500,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}

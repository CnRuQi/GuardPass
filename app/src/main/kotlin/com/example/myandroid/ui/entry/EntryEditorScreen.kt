package com.example.myandroid.ui.entry

import android.content.Context

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myandroid.R
import com.example.myandroid.data.db.entity.PasswordEntry
import com.example.myandroid.ui.component.AppSectionHeader
import com.example.myandroid.ui.component.InlineStatus
import com.example.myandroid.ui.component.SecureClipboard
import com.example.myandroid.ui.component.StatusTone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorScreen(
    entryId: Long?,
    onBack: () -> Unit,
    viewModel: EntryEditorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categories by viewModel.categories.observeAsState(emptyList())
    var secretVisible by rememberSaveable(entryId) { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(entryId) { viewModel.load(entryId) }
    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onBack()
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(if (entryId == null) "添加记录" else "编辑记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = "返回")
                    }
                },
                actions = {
                    if (entryId != null) {
                        IconButton(onClick = { showDeleteConfirmation = true }, enabled = !state.isDeleting) {
                            Icon(painterResource(R.drawable.ic_delete), contentDescription = "删除记录", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(contentPadding).imePadding()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
            item {
                AppSectionHeader("记录类型", "选择保存方式")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.entryType == PasswordEntry.TYPE_PASSWORD,
                        onClick = { viewModel.setType(PasswordEntry.TYPE_PASSWORD) },
                        label = { Text("密码") }
                    )
                    FilterChip(
                        selected = state.entryType == PasswordEntry.TYPE_API_KEY,
                        onClick = { viewModel.setType(PasswordEntry.TYPE_API_KEY) },
                        label = { Text("API Key") }
                    )
                }
            }
            item {
                AppSectionHeader("基本信息")
                FormField(
                    value = state.title,
                    onValueChange = viewModel::setTitle,
                    label = "标题",
                    error = state.fieldErrors["title"]
                )
                if (state.entryType == PasswordEntry.TYPE_PASSWORD) {
                    FormField(
                        value = state.username,
                        onValueChange = viewModel::setUsername,
                        label = "账号",
                        error = state.fieldErrors["username"],
                        keyboardType = KeyboardType.Email
                    )
                }
            }
            item {
                AppSectionHeader("密钥", if (state.entryType == PasswordEntry.TYPE_API_KEY) "API Key 会以加密形式保存" else "密码会以加密形式保存")
                OutlinedTextField(
                    value = state.secret,
                    onValueChange = viewModel::setSecret,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (state.entryType == PasswordEntry.TYPE_API_KEY) "API Key" else "密码") },
                    isError = state.fieldErrors.containsKey("secret"),
                    singleLine = true,
                    visualTransformation = if (secretVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        Row {
                            IconButton(
                                onClick = { SecureClipboard.copy(context, "密钥", state.secret, scope) },
                                enabled = state.secret.isNotBlank()
                            ) {
                                Icon(painterResource(R.drawable.ic_copy), contentDescription = "复制密钥")
                            }
                            IconButton(onClick = { secretVisible = !secretVisible }) {
                                Icon(
                                    painterResource(if (secretVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                                    contentDescription = if (secretVisible) "隐藏密钥" else "显示密钥"
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                state.fieldErrors["secret"]?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                if (state.entryType == PasswordEntry.TYPE_PASSWORD && state.secret.isNotBlank()) {
                    Text(
                        passwordStrength(state.secret),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            item {
                AppSectionHeader("分类与附加信息")
                CategoryPicker(categories = categories, selectedId = state.categoryId, onSelect = viewModel::setCategory)
                Spacer(Modifier.width(1.dp))
                FormField(
                    value = state.url,
                    onValueChange = viewModel::setUrl,
                    label = "网址（可选）",
                    keyboardType = KeyboardType.Uri
                )
                FormField(
                    value = state.notes,
                    onValueChange = viewModel::setNotes,
                    label = "备注（可选）",
                    singleLine = false,
                    minLines = 4
                )
            }
            item {
                state.error?.let { InlineStatus(it, StatusTone.Error) }
            }
            }

            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving && !state.isDeleting,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(if (state.isSaving) "保存中…" else "保存记录")
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("删除这条记录？") },
            text = { Text("删除后无法恢复。") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirmation = false; viewModel.delete() }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        label = { Text(label) },
        isError = error != null,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = error?.let { { Text(it) } }
    )
}

@Composable
private fun CategoryPicker(
    categories: List<com.example.myandroid.data.db.entity.Category>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedId }?.name ?: "无分类"
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("分类") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(painterResource(R.drawable.ic_more_vert), contentDescription = "选择分类")
                }
            }
        )
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("无分类") },
                onClick = { onSelect(null); expanded = false }
            )
            categories.forEach { category ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(category.name.orEmpty()) },
                    onClick = { onSelect(category.id); expanded = false }
                )
            }
        }
    }
}

private fun passwordStrength(secret: String): String {
    val score = listOf(
        secret.length >= 12,
        secret.any(Char::isUpperCase),
        secret.any(Char::isLowerCase),
        secret.any(Char::isDigit),
        secret.any { !it.isLetterOrDigit() }
    ).count { it }
    return when {
        score <= 2 -> "密码强度：较弱"
        score <= 3 -> "密码强度：一般"
        else -> "密码强度：较强"
    }
}

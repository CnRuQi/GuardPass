package com.example.myandroid.ui.add

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.myandroid.data.db.entity.Category
import com.example.myandroid.data.db.entity.PasswordEntry
import com.example.myandroid.ui.component.*
import com.example.myandroid.ui.theme.*
import com.example.myandroid.util.PasswordStrengthUtil

@Composable
fun AddEditScreen(
    passwordId: Long = -1,
    onBack: () -> Unit,
    viewModel: AddEditViewModel = viewModel()
) {
    val context = LocalContext.current
    val categories by viewModel.allCategories.observeAsState(emptyList())
    val loadedEntry by viewModel.loadedEntry.observeAsState()
    val saveResult by viewModel.saveResult.observeAsState()
    val deleteResult by viewModel.deleteResult.observeAsState()

    val isEditMode = passwordId != -1L

    // Form state
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var entryType by remember { mutableStateOf(PasswordEntry.TYPE_PASSWORD) }
    var passwordVisible by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    // Load entry if editing
    LaunchedEffect(passwordId) {
        if (isEditMode) {
            viewModel.loadPassword(passwordId)
        }
    }

    // Populate form when entry loaded
    LaunchedEffect(loadedEntry) {
        loadedEntry?.let { entry ->
            title = entry.title ?: ""
            username = entry.username ?: ""
            password = entry.password ?: ""
            url = entry.url ?: ""
            notes = entry.notes ?: ""
            entryType = entry.entryType
            val cat = categories.find { it.id == entry.categoryId }
            selectedCategory = cat
        }
    }

    // Handle save result
    LaunchedEffect(saveResult) {
        saveResult?.let { success ->
            if (success) {
                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                onBack()
            }
        }
    }

    // Handle delete result
    LaunchedEffect(deleteResult) {
        deleteResult?.let { success ->
            if (success) {
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                onBack()
            }
        }
    }

    // Password strength
    val strength = remember(password) {
        if (password.isNotEmpty()) PasswordStrengthUtil.calculate(password) else null
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
                    Text(
                        text = "‹",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                Text(
                    text = if (isEditMode) "编辑记录" else "添加记录",
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )

                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .springScaleClickable(scaleDownTo = 0.85f) {
                                showDeleteConfirm = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "删除",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.width(4.dp))

                GlassTextButton(
                    text = "保存",
                    onClick = {
                        titleError = title.isBlank()
                        usernameError = username.isBlank()
                        if (!titleError && !usernameError) {
                            viewModel.savePassword(
                                title, username,
                                if (entryType == PasswordEntry.TYPE_API_KEY) "" else password,
                                url, notes,
                                selectedCategory?.id, entryType
                            )
                        }
                    },
                    textColor = Purple500,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16
                )
            }

            // ── Form Content ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Entry type toggle
                SectionLabel("类型")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip(
                        text = "密码",
                        selected = entryType == PasswordEntry.TYPE_PASSWORD,
                        onClick = { entryType = PasswordEntry.TYPE_PASSWORD },
                        modifier = Modifier.weight(1f)
                    )
                    GlassChip(
                        text = "API Key",
                        selected = entryType == PasswordEntry.TYPE_API_KEY,
                        onClick = { entryType = PasswordEntry.TYPE_API_KEY },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Category selector
                SectionLabel("分类")
                Spacer(Modifier.height(8.dp))
                GlassCategorySelector(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSelect = { selectedCategory = it },
                    expanded = showCategoryDropdown,
                    onToggle = { showCategoryDropdown = !showCategoryDropdown }
                )

                Spacer(Modifier.height(16.dp))

                // Title
                SectionLabel("主题名称")
                Spacer(Modifier.height(8.dp))
                GlassTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    placeholder = "请输入主题名称",
                    isError = titleError,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
                if (titleError) {
                    Text("请输入主题名称", color = ErrorRed, fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                Spacer(Modifier.height(12.dp))

                // Username
                SectionLabel(if (entryType == PasswordEntry.TYPE_API_KEY) "API Key" else "账号")
                Spacer(Modifier.height(8.dp))
                GlassTextField(
                    value = username,
                    onValueChange = { username = it; usernameError = false },
                    placeholder = if (entryType == PasswordEntry.TYPE_API_KEY) "请输入 API Key" else "请输入账号",
                    isError = usernameError,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
                if (usernameError) {
                    Text("请输入${if (entryType == PasswordEntry.TYPE_API_KEY) "API Key" else "账号"}", color = ErrorRed, fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                // Password fields (only for password type)
                AnimatedVisibility(visible = entryType == PasswordEntry.TYPE_PASSWORD) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        SectionLabel("密码")
                        Spacer(Modifier.height(8.dp))
                        GlassTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "请输入密码",
                            keyboardType = KeyboardType.Password,
                            passwordVisible = passwordVisible,
                            imeAction = ImeAction.Next,
                            trailingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .springScaleClickable(scaleDownTo = 0.8f) {
                                            passwordVisible = !passwordVisible
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            if (passwordVisible) R.drawable.ic_visibility_off
                                            else R.drawable.ic_visibility
                                        ),
                                        contentDescription = "切换",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )

                        // Strength indicator
                        strength?.let { s ->
                            Spacer(Modifier.height(8.dp))
                            PasswordStrengthBar(strength = s)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // URL
                SectionLabel("网址（可选）")
                Spacer(Modifier.height(8.dp))
                GlassTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = "请输入网址",
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                )

                Spacer(Modifier.height(12.dp))

                // Notes
                SectionLabel("备注（可选）")
                Spacer(Modifier.height(8.dp))
                GlassTextFieldMultiline(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "请输入备注",
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }

    // Delete confirmation
    GlassDialog(
        visible = showDeleteConfirm,
        onDismiss = { showDeleteConfirm = false },
        title = "确认删除",
        message = "确定要删除这条记录吗？此操作不可撤销。",
        confirmText = "删除",
        cancelText = "取消",
        onConfirm = { viewModel.deletePassword() },
        confirmDestructive = true
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun GlassCategorySelector(
    categories: List<Category>,
    selectedCategory: Category?,
    onSelect: (Category) -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.35f),
            borderColor = Color.White.copy(alpha = 0.3f),
            padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            onClick = onToggle
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCategory?.name ?: "无分类",
                    color = if (selectedCategory != null) TextPrimary else TextSecondary,
                    fontSize = 16.sp,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = if (expanded) "▲" else "▼",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                categories.forEach { cat ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.3f),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        onClick = {
                            onSelect(cat)
                            onToggle()
                        }
                    ) {
                        Text(
                            text = cat.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            letterSpacing = (-0.2).sp
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun PasswordStrengthBar(strength: PasswordStrengthUtil.Strength) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "密码强度",
            color = TextTertiary,
            fontSize = 12.sp
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.3f), CardShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(PasswordStrengthUtil.getProgress(strength) / 100f)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(strength.color),
                                Color(strength.color).copy(alpha = 0.6f)
                            )
                        ),
                        CardShape
                    )
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = strength.label,
            color = Color(strength.color),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

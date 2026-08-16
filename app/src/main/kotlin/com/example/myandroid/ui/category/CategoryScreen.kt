package com.example.myandroid.ui.category

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myandroid.R
import com.example.myandroid.data.db.entity.Category
import com.example.myandroid.ui.component.*
import com.example.myandroid.ui.theme.*

@Composable
fun CategoryScreen(
    onBack: () -> Unit,
    viewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val categories by viewModel.allCategories.observeAsState(emptyList())
    val counts by viewModel.categoryCounts.observeAsState(emptyMap())
    val addResult by viewModel.addResult.observeAsState()
    val editResult by viewModel.editResult.observeAsState()
    val deleteResult by viewModel.deleteResult.observeAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Category?>(null) }

    // Handle results
    LaunchedEffect(addResult) {
        addResult?.let { success ->
            Toast.makeText(context,
                if (success) "分类添加成功" else "分类已存在",
                Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(editResult) {
        editResult?.let { success ->
            Toast.makeText(context,
                if (success) "分类编辑成功" else "分类已存在",
                Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(deleteResult) {
        deleteResult?.let { success ->
            if (success) {
                Toast.makeText(context, "分类删除成功", Toast.LENGTH_SHORT).show()
            }
        }
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
                    text = "分类管理",
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.ic_empty_box),
                            contentDescription = null,
                            tint = TextTertiary.copy(alpha = 0.4f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("暂无分类", color = TextSecondary, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            count = counts[category.id] ?: 0,
                            onEdit = { editingCategory = category },
                            onDelete = { showDeleteConfirm = category }
                        )
                    }
                }
            }

            Spacer(Modifier.height(60.dp))
        }

        // ── FAB (in Box scope for .align) ──
        GlassIconButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .navigationBarsPadding(),
            size = 60.dp,
            backgroundColor = Purple500
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = "添加分类",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // ── Add Dialog ──
    CategoryInputDialog(
        visible = showAddDialog,
        title = "添加分类",
        onDismiss = { showAddDialog = false },
        onConfirm = { name -> viewModel.addCategory(name) }
    )

    // ── Edit Dialog ──
    if (editingCategory != null) {
        val cat = editingCategory!!
        CategoryInputDialog(
            visible = true,
            title = "编辑分类",
            initialValue = cat.name,
            onDismiss = { editingCategory = null },
            onConfirm = { name ->
                viewModel.updateCategory(cat, name)
                editingCategory = null
            }
        )
    }

    // ── Delete Confirm ──
    if (showDeleteConfirm != null) {
        GlassDialog(
            visible = true,
            onDismiss = { showDeleteConfirm = null },
            title = "确认删除",
            message = "确定要删除这个分类吗？分类下的密码记录将变为无分类。",
            confirmText = "删除",
            cancelText = "取消",
            onConfirm = {
                showDeleteConfirm?.let { viewModel.deleteCategory(it) }
                showDeleteConfirm = null
            },
            confirmDestructive = true
        )
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    count: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.45f),
        borderColor = Color.White.copy(alpha = 0.6f),
        padding = PaddingValues(16.dp)
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
                    painter = painterResource(R.drawable.ic_category),
                    contentDescription = null,
                    tint = Purple500,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "${count} 条记录",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    letterSpacing = (-0.2).sp
                )
            }

            // Edit button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .springScaleClickable(scaleDownTo = 0.8f, onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = "编辑",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .springScaleClickable(scaleDownTo = 0.8f, onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "删除",
                    tint = ErrorRed.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryInputDialog(
    visible: Boolean,
    title: String,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember(visible) { mutableStateOf(initialValue) }
    var error by remember(visible) { mutableStateOf<String?>(null) }

    GlassInputDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = title,
        inputValue = name,
        onInputChange = { name = it; error = null },
        inputPlaceholder = "分类名称",
        confirmText = "确定",
        cancelText = "取消",
        onConfirm = {
            if (name.isBlank()) {
                error = "请输入分类名称"
            } else {
                onConfirm(name.trim())
            }
        },
        inputError = error
    )
}

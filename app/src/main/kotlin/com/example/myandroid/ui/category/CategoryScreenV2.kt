package com.example.myandroid.ui.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myandroid.R
import com.example.myandroid.data.db.entity.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreenV2(
    contentPadding: PaddingValues,
    viewModel: CategoryViewModel = viewModel()
) {
    val categories by viewModel.allCategories.observeAsState(emptyList())
    val counts by viewModel.categoryCounts.observeAsState(emptyMap())
    val addResult by viewModel.addResult.observeAsState()
    val editResult by viewModel.editResult.observeAsState()
    val deleteResult by viewModel.deleteResult.observeAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Category?>(null) }
    var deleting by remember { mutableStateOf<Category?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var noticeIsError by remember { mutableStateOf(false) }

    LaunchedEffect(addResult) {
        addResult?.let {
            noticeIsError = !it
            notice = if (it) "分类已添加" else "分类名称已存在或无效"
        }
    }
    LaunchedEffect(editResult) {
        editResult?.let {
            noticeIsError = !it
            notice = if (it) "分类已更新" else "分类名称已存在或无效"
        }
    }
    LaunchedEffect(deleteResult) {
        deleteResult?.let {
            noticeIsError = !it
            notice = if (it) "分类已删除，记录已变为未分类" else "删除失败"
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("分类") },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
            if (notice != null) {
                Text(
                    notice.orEmpty(),
                    color = if (noticeIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (categories.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("还没有分类", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryRow(
                            category = category,
                            count = counts[category.id] ?: 0,
                            onEdit = { editing = category },
                            onDelete = { deleting = category }
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = "添加分类")
        }
    }

    if (showAdd) {
        CategoryDialog(
            title = "添加分类",
            onDismiss = { showAdd = false },
            onConfirm = { viewModel.addCategory(it); showAdd = false }
        )
    }
    editing?.let { category ->
        CategoryDialog(
            title = "编辑分类",
            initialValue = category.name.orEmpty(),
            onDismiss = { editing = null },
            onConfirm = { viewModel.updateCategory(category, it); editing = null }
        )
    }
    deleting?.let { category ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("删除分类？") },
            text = { Text("分类下的记录不会被删除，会变为未分类。") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCategory(category); deleting = null }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun CategoryRow(category: Category, count: Int, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.ic_category), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(category.name.orEmpty(), style = MaterialTheme.typography.titleMedium)
                Text("$count 条记录", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onEdit) { Icon(painterResource(R.drawable.ic_edit), contentDescription = "编辑分类") }
            IconButton(onClick = onDelete) { Icon(painterResource(R.drawable.ic_delete), contentDescription = "删除分类", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember(title, initialValue) { mutableStateOf(initialValue) }
    var error by remember(title, initialValue) { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it; error = false },
                label = { Text("分类名称") },
                singleLine = true,
                isError = error,
                supportingText = if (error) ({ Text("请输入分类名称") }) else null
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (value.isBlank()) error = true else onConfirm(value.trim())
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

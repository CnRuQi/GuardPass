package com.example.myandroid.ui.vault

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myandroid.R
import com.example.myandroid.ui.component.InlineStatus
import com.example.myandroid.ui.component.PasswordListItem
import com.example.myandroid.ui.component.SecureClipboard
import com.example.myandroid.ui.component.StatusTone
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    contentPadding: PaddingValues,
    onOpenSearch: () -> Unit,
    onOpenEditor: (Long?) -> Unit,
    initialCategoryId: Long? = null,
    screenTitle: String = "密码库",
    viewModel: VaultViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialCategoryId) {
        viewModel.selectCategory(initialCategoryId)
    }

    LaunchedEffect(actionError) {
        if (actionError != null) {
            delay(3_000)
            viewModel.clearActionError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(screenTitle) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "搜索"
                        )
                    }
                }
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        state.totalCount.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "条记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 7.dp)
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("全部") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedCategoryId == VaultViewModel.FAVORITES_FILTER,
                            onClick = { viewModel.selectCategory(VaultViewModel.FAVORITES_FILTER) },
                            label = { Text("收藏") }
                        )
                    }
                    items(state.categories, key = { it.id }) { category ->
                        FilterChip(
                            selected = state.selectedCategoryId == category.id,
                            onClick = { viewModel.selectCategory(category.id) },
                            label = { Text(category.name.orEmpty()) }
                        )
                    }
                }
            }

            if (actionError != null) {
                InlineStatus(actionError.orEmpty(), StatusTone.Error, Modifier.padding(horizontal = 16.dp))
            }

            if (state.error != null) {
                InlineStatus(state.error.orEmpty(), StatusTone.Error, Modifier.padding(horizontal = 16.dp))
            }

            if (state.error != null) {
                Spacer(Modifier.weight(1f))
            } else if (state.entries.isEmpty()) {
                EmptyVault(onAdd = { onOpenEditor(null) }, modifier = Modifier.fillMaxWidth().weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(state.entries, key = { _, entry -> entry.id }) { _, entry ->
                        PasswordListItem(
                            entry = entry,
                            onOpen = { onOpenEditor(entry.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(entry) },
                            onCopyUsername = { SecureClipboard.copy(context, "账号", entry.username.orEmpty(), scope) },
                            onCopySecret = { SecureClipboard.copy(context, "密钥", entry.password.orEmpty(), scope) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onOpenEditor(null) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = "添加记录")
        }
    }
}

@Composable
private fun EmptyVault(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_empty_box),
                contentDescription = "暂无密码记录",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Text("还没有密码记录", style = MaterialTheme.typography.titleLarge)
            Text(
                "把常用账号和密钥集中到这里",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AssistChip(onClick = onAdd, label = { Text("添加第一条记录") })
        }
    }
}

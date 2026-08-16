package com.example.myandroid.ui.search

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myandroid.R
import com.example.myandroid.data.db.entity.PasswordEntry
import com.example.myandroid.ui.add.AddEditActivity
import com.example.myandroid.ui.component.*
import com.example.myandroid.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val results by viewModel.searchResults.observeAsState()
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
            // ── Search Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.3f))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
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

                // Search input
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .padding(start = 8.dp)
                        .clip(TextFieldShape)
                        .background(Color.White.copy(alpha = 0.4f), TextFieldShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.5f), TextFieldShape)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = query,
                        onValueChange = { newQuery ->
                            query = newQuery
                            debounceJob?.cancel()
                            debounceJob = scope.launch {
                                delay(300)
                                viewModel.setSearchQuery(newQuery)
                            }
                        },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = TextPrimary,
                            fontSize = 16.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        cursorBrush = SolidColor(Purple500),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        decorationBox = { innerTextField ->
                            Box {
                                if (query.isEmpty()) {
                                    Text(
                                        "输入主题、账号或网址搜索...",
                                        color = TextSecondary,
                                        fontSize = 16.sp,
                                        letterSpacing = (-0.2).sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            // ── Results ──
            when {
                results == null -> {
                    // No search yet
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "输入关键词开始搜索...",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                results!!.isEmpty() -> {
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
                            Text(
                                text = "未找到匹配结果",
                                color = TextSecondary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                else -> {
                    val resultList = results ?: emptyList()
                    Text(
                        text = "找到 ${resultList.size} 条结果",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, bottom = 40.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(resultList, key = { it.id }) { entry ->
                            SearchResultCard(
                                entry = entry,
                                onClick = {
                                    val intent = Intent(context, AddEditActivity::class.java)
                                    intent.putExtra("password_id", entry.id)
                                    context.startActivity(intent)
                                },
                                onCopyUsername = {
                                    copyToClipboard(context, "用户名", entry.username)
                                },
                                onCopyPassword = {
                                    copyToClipboard(context, "密码", entry.password)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    entry: PasswordEntry,
    onClick: () -> Unit,
    onCopyUsername: () -> Unit,
    onCopyPassword: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.45f),
        borderColor = Color.White.copy(alpha = 0.6f),
        padding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Purple500.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    tint = Purple500,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

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
                Text(
                    text = entry.username,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Copy buttons
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .springScaleClickable(scaleDownTo = 0.8f, onClick = onCopyUsername),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_copy),
                    contentDescription = "复制账号",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
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

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}

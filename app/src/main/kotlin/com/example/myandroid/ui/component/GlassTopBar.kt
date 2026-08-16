package com.example.myandroid.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myandroid.R
import com.example.myandroid.ui.theme.*

/**
 * VisionOS-style floating top bar — no Material TopAppBar used.
 *
 * Semi-transparent background with blur effect (where supported),
 * large title, and optional trailing action.
 */
@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailingContent: @Composable RowScope.() -> Unit = {},
    titleFontSize: Int = 28,
    backgroundColor: Color = Color.White.copy(alpha = 0.35f)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(ChipShape)
                    .springScaleClickable(scaleDownTo = 0.85f, onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                // Inline back arrow drawn with Canvas, or we can use a simple text chevron
                Text(
                    text = "‹",   // single left-pointing angle quotation mark
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light
                )
            }
            Spacer(Modifier.width(12.dp))
        }

        Text(
            text = title,
            color = TextPrimary.copy(alpha = 0.85f),
            fontSize = titleFontSize.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp,
            modifier = Modifier.weight(1f)
        )

        trailingContent()
    }
}

/**
 * Compact top bar for detail/form screens with a centered title.
 */
@Composable
fun GlassTopBarCompact(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailingContent: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = Color.White.copy(alpha = 0.35f)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(ChipShape)
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
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = title,
            color = TextPrimary.copy(alpha = 0.85f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
            modifier = Modifier.weight(1f).padding(start = if (onBack == null) 8.dp else 0.dp)
        )

        trailingContent()
    }
}

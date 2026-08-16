package com.example.myandroid.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myandroid.ui.theme.*

/**
 * VisionOS-style primary button — pill-shaped, gradient fill, spring scale.
 * No ripple, no Material Button component used.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: RoundedCornerShape = ButtonShape,
    backgroundColor: Brush = Brush.linearGradient(
        listOf(Purple500, Purple600)
    ),
    disabledBackground: Brush = Brush.linearGradient(
        listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.2f))
    ),
    textColor: Color = Color.White,
    disabledTextColor: Color = Color.White.copy(alpha = 0.5f),
    height: Dp = 52.dp,
    fontSize: Int = 16,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    val bg = if (enabled) backgroundColor else disabledBackground
    val fg = if (enabled) textColor else disabledTextColor

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .clip(shape)
            .background(bg, shape)
            .springScaleClickable(
                enabled = enabled,
                scaleDownTo = 0.95f,
                onClick = onClick
            )
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.3).sp
        )
    }
}

/**
 * VisionOS-style outlined/ghost button — transparent with fine border.
 */
@Composable
fun GlassButtonOutlined(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = ButtonShape,
    borderColor: Color = Color.White.copy(alpha = 0.4f),
    backgroundColor: Color = Color.White.copy(alpha = 0.15f),
    textColor: Color = TextPrimary,
    height: Dp = 48.dp,
    fontSize: Int = 15,
    fontWeight: FontWeight = FontWeight.Medium
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .clip(shape)
            .background(backgroundColor, shape)
            .border(0.5.dp, borderColor, shape)
            .springScaleClickable(
                scaleDownTo = 0.96f,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.2).sp
        )
    }
}

/**
 * Minimal text button — just text, spring scale, no background.
 */
@Composable
fun GlassTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Purple500,
    fontSize: Int = 16,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .springScaleClickable(
                scaleDownTo = 0.94f,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            letterSpacing = (-0.2).sp
        )
    }
}

/**
 * Circular icon button — FAB replacement, glass style.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    backgroundColor: Color = Purple500,
    iconColor: Color = Color.White,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .springScaleClickable(
                scaleDownTo = 0.88f,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

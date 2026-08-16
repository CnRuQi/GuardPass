package com.example.myandroid.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myandroid.ui.theme.CardShape

/**
 * VisionOS glass card — the fundamental surface container.
 *
 * Features:
 * - Semi-transparent background (glassmorphism)
 * - Fine white inner border (glass edge highlight)
 * - Optional soft shadow for depth
 * - Large apple-style rounded corners (24dp)
 * - No Material Card component used!
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = CardShape,
    backgroundColor: Color = Color.White.copy(alpha = 0.4f),
    borderColor: Color = Color.White.copy(alpha = 0.5f),
    borderWidth: Dp = 0.5.dp,
    shadowColor: Color = Color.Black.copy(alpha = 0.04f),
    shadowElevation: Dp = 0.dp,
    shadowRadius: Dp = 24.dp,
    padding: PaddingValues = PaddingValues(20.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val baseModifier = modifier
        .then(
            if (shadowElevation > 0.dp) {
                Modifier.shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
            } else Modifier
        )
        .clip(shape)
        .background(backgroundColor, shape)
        .border(borderWidth, borderColor, shape)

    val finalModifier = if (onClick != null) {
        baseModifier.springScaleClickable(onClick = onClick)
    } else baseModifier

    Box(
        modifier = finalModifier
            .padding(padding),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

/**
 * Glass card with a gradient background for extra depth dimension.
 * Uses a subtle diagonal linear gradient.
 */
@Composable
fun GlassCardGradient(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = CardShape,
    gradient: Brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.35f)
        )
    ),
    borderColor: Color = Color.White.copy(alpha = 0.6f),
    borderWidth: Dp = 0.5.dp,
    padding: PaddingValues = PaddingValues(20.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val baseModifier = modifier
        .clip(shape)
        .background(gradient, shape)
        .border(borderWidth, borderColor, shape)

    val finalModifier = if (onClick != null) {
        baseModifier.springScaleClickable(onClick = onClick)
    } else baseModifier

    Box(
        modifier = finalModifier.padding(padding),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

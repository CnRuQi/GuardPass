package com.example.myandroid.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import com.example.myandroid.ui.theme.SpringBouncy

/**
 * Applies iOS-style spring scale animation on press.
 * When pressed: scale to 0.96f, when released: spring back to 1.0f.
 * No ripple — press feedback is purely through scale + optional visual.
 *
 * Usage:
 * ```
 * Modifier.springScaleClickable(onClick = { ... }) {
 *     // your content
 * }
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.springScaleClickable(
    enabled: Boolean = true,
    scaleDownTo: Float = 0.96f,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDownTo else 1f,
        animationSpec = SpringBouncy,
        label = "springScale"
    )

    return this
        .scale(scale)
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,  // NO ripple!
            enabled = enabled,
            onClick = onClick,
            onLongClick = onLongClick
        )
}

/**
 * Simpler version: just the scale observable without click handling.
 * Use this when you need custom click handling with Modifier.clickable {}.
 */
@Composable
fun Modifier.springScale(
    pressed: Boolean,
    scaleDownTo: Float = 0.96f
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) scaleDownTo else 1f,
        animationSpec = SpringBouncy,
        label = "springScale"
    )
    return this.scale(scale)
}

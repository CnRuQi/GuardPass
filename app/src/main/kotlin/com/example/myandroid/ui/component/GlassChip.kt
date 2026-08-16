package com.example.myandroid.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myandroid.ui.theme.*

/**
 * VisionOS-style pill chip — used for category filters.
 * Selected: filled brand color. Unselected: translucent gray.
 * Spring scale animation on press, NO ripple.
 */
@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = ChipShape,
    selectedColor: Color = ChipSelected,
    unselectedColor: Color = ChipUnselected,
    selectedTextColor: Color = ChipSelectedText,
    unselectedTextColor: Color = TextPrimary
) {
    val bg = if (selected) selectedColor else unselectedColor
    val fg = if (selected) selectedTextColor else unselectedTextColor

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(bg, shape)
            .springScaleClickable(
                scaleDownTo = 0.92f,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            letterSpacing = (-0.2).sp,
            maxLines = 1
        )
    }
}

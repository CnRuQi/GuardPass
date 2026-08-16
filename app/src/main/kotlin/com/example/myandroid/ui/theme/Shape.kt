package com.example.myandroid.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// VisionOS / iOS 28 large-radius shapes — no sharp corners anywhere

val VisionOSShapes = Shapes(
    // Cards, dialogs, sheets
    extraLarge = RoundedCornerShape(36.dp),
    large = RoundedCornerShape(28.dp),
    // Standard card corners
    medium = RoundedCornerShape(24.dp),
    // Smaller elements (chips, inline cards)
    small = RoundedCornerShape(16.dp),
    // Minimal rounding (text fields, small panels)
    extraSmall = RoundedCornerShape(12.dp)
)

// Convenience aliases for common shapes
val CardShape = RoundedCornerShape(24.dp)
val CardShapeLarge = RoundedCornerShape(28.dp)
val ButtonShape = RoundedCornerShape(16.dp)
val ChipShape = RoundedCornerShape(999.dp)  // pill
val TextFieldShape = RoundedCornerShape(16.dp)
val DialogShape = RoundedCornerShape(36.dp)
val CircleButtonShape = CircleShape

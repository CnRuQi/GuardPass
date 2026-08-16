package com.example.myandroid.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom color scheme — we use our own colors, not M3 defaults,
// because we want VisionOS glassmorphism, not Material look.

private val VisionOSLightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = Color.White,
    primaryContainer = Purple100,
    onPrimaryContainer = Purple600,
    secondary = Purple400,
    onSecondary = Color.White,
    secondaryContainer = Purple50,
    onSecondaryContainer = Purple500,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFE0E0E5),
    outlineVariant = Color(0x1A000000),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFE0E0),
    onErrorContainer = Color(0xFFCC2F2A)
)

private val VisionOSDarkColorScheme = darkColorScheme(
    primary = Purple400,
    onPrimary = Color.White,
    primaryContainer = Purple600,
    onPrimaryContainer = Purple100,
    secondary = Purple400,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3A3866),
    onSecondaryContainer = Purple100,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF3A3A3C),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF48484A),
    outlineVariant = Color(0x1AFFFFFF),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF4A2020),
    onErrorContainer = Color(0xFFFFB3B0)
)

@Composable
fun VisionOSPasswordManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VisionOSDarkColorScheme else VisionOSLightColorScheme

    // Edge-to-edge: transparent system bars
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VisionOSTypography,
        shapes = VisionOSShapes,
        content = content
    )
}

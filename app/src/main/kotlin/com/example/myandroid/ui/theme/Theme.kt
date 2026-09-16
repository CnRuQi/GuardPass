package com.example.myandroid.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GuardPassLightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealSoft,
    onPrimaryContainer = TealDark,
    secondary = InkMuted,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EAE7),
    onSecondaryContainer = Ink,
    tertiary = Amber,
    onTertiary = Color.White,
    tertiaryContainer = AmberSoft,
    onTertiaryContainer = Color(0xFF3A2900),
    background = Paper,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE9EFEC),
    onSurfaceVariant = InkMuted,
    outline = Line,
    outlineVariant = Color(0xFFEDF1EF),
    error = Danger,
    onError = Color.White,
    errorContainer = DangerSoft,
    onErrorContainer = Color(0xFF410003)
)

private val GuardPassDarkColorScheme = darkColorScheme(
    primary = Color(0xFF75CFC3),
    onPrimary = Color(0xFF003A35),
    primaryContainer = TealDark,
    onPrimaryContainer = TealSoft,
    secondary = Color(0xFFB5C6C1),
    onSecondary = Color(0xFF20302D),
    secondaryContainer = Color(0xFF354540),
    onSecondaryContainer = Color(0xFFD1E1DC),
    tertiary = Color(0xFFE7BD65),
    onTertiary = Color(0xFF3D2E00),
    tertiaryContainer = Color(0xFF594500),
    onTertiaryContainer = Color(0xFFFFE9B8),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF34433F),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF81918B),
    outlineVariant = Color(0xFF3D4B47),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun VisionOSPasswordManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) GuardPassDarkColorScheme else GuardPassLightColorScheme,
        typography = VisionOSTypography,
        shapes = VisionOSShapes,
        content = content
    )
}

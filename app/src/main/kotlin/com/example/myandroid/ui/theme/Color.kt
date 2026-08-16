package com.example.myandroid.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// VisionOS / iOS 28 Glassmorphism Color Palette
// 枯木冷茶 · 禅茶书房 — 弥散渐变 + 毛玻璃
// ============================================================

// ── Brand / Primary ──
val Purple500 = Color(0xFF5856D6)
val Purple600 = Color(0xFF4A48B5)
val Purple400 = Color(0xFF7A78E0)
val Purple50 = Color(0xFFE8E6FF)
val Purple100 = Color(0xFFF2F0FF)

// ── Background Layers (莫兰迪灰调) ──
val BackgroundLight = Color(0xFFF2F2F7)
val BackgroundDark = Color(0xFF1C1C1E)
val SurfaceLight = Color(0xFFF8F8FC)
val SurfaceDark = Color(0xFF2C2C2E)

// ── Glass / Card backgrounds ──
val GlassWhite = Color(0x66FFFFFF)   // ~40% white glass
val GlassDark = Color(0x661C1C1E)    // ~40% dark glass
val GlassBorder = Color(0x80FFFFFF)  // ~50% white inner border
val GlassBorderDark = Color(0x33FFFFFF)

// ── Text ──
val TextPrimary = Color(0xFF1C1C1E)
val TextSecondary = Color(0xFF6E6E73)
val TextTertiary = Color(0xFFAEAEB2)
val TextPrimaryDark = Color(0xFFF2F2F7)
val TextSecondaryDark = Color(0xFF8E8E93)
val TextTertiaryDark = Color(0xFF636366)

// ── Semantic ──
val ErrorRed = Color(0xFFFF3B30)
val SuccessGreen = Color(0xFF34C759)
val WarningOrange = Color(0xFFFF9F0A)
val InfoBlue = Color(0xFF007AFF)

// ── Password Mask ──
val PasswordMask = Color(0xFFC7C7CC)
val PasswordMaskDark = Color(0xFF48484A)

// ── Gradient stops for diffuse backgrounds ──
val GradientStart = Color(0xFFE8E0F0)   // soft lavender
val GradientMid = Color(0xFFF0E8E8)     // warm blush
val GradientEnd = Color(0xFFE8F0F0)     // cool mint whisper

// ── Chip colors ──
val ChipUnselected = Color(0x4D8E8E93)  // 30% gray
val ChipSelected = Purple500
val ChipSelectedText = Color.White
val ChipUnselectedText = TextPrimary

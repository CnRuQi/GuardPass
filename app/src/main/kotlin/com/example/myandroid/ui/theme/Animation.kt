package com.example.myandroid.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

// ============================================================
// VisionOS-style animation specs — spring-driven, elastic, fluid
// ============================================================

/**
 * iOS-style spring for button/card press scale effect.
 * DampingRatioMediumBouncy + StiffnessLow gives the iconic
 * "slightly bouncy" feel of Apple interactions.
 */
val SpringBouncy = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

/**
 * A snappier spring for less playful transitions (e.g., switches).
 */
val SpringSnappy = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

/**
 * Smooth, non-bouncy tween for opacity/color transitions.
 */
val TweenSmooth = tween<Float>(
    durationMillis = 280,
    easing = androidx.compose.animation.core.FastOutSlowInEasing
)

/**
 * Duration constants matching the original dimens.xml motion values.
 */
object MotionDuration {
    const val XS = 120
    const val SM = 200
    const val MD = 280
    const val LG = 380
    const val XL = 480
}

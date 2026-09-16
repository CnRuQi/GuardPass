package com.example.myandroid.ui.theme

import androidx.compose.animation.core.tween

// Short, non-bouncy transitions keep the secure workspace calm and predictable.
val SpringBouncy = tween<Float>(durationMillis = 160)
val SpringSnappy = tween<Float>(durationMillis = 120)
val TweenSmooth = tween<Float>(durationMillis = 180)

object MotionDuration {
    const val XS = 100
    const val SM = 160
    const val MD = 220
    const val LG = 300
    const val XL = 420
}

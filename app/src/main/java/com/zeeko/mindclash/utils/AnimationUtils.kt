package com.zeeko.mindclash.utils

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AnimationUtils {
    
    @Composable
    fun rememberInfiniteShimmerBrush(
        colors: List<Color> = listOf(
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.5f),
            Color.White.copy(alpha = 0.3f)
        )
    ): Brush {
        val transition = rememberInfiniteTransition()
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        
        return Brush.linearGradient(
            colors = colors,
            start = Offset(translateAnimation, translateAnimation),
            end = Offset(translateAnimation + 200f, translateAnimation + 200f)
        )
    }
    
    @Composable
    fun rememberPulseAnimation(
        initialValue: Float = 1f,
        targetValue: Float = 1.2f,
        duration: Int = 1000
    ): Float {
        val transition = rememberInfiniteTransition()
        return transition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        ).value
    }
    
    @Composable
    fun rememberRotationAnimation(
        duration: Int = 2000
    ): Float {
        val transition = rememberInfiniteTransition()
        return transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        ).value
    }
}

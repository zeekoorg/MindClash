package com.zeeko.mindclash.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    val infiniteTransition = rememberInfiniteTransition()
    
    val circle1X by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = screenWidth.value,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val circle1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = screenHeight.value,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val circle2X by infiniteTransition.animateFloat(
        initialValue = screenWidth.value,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val circle2Y by infiniteTransition.animateFloat(
        initialValue = screenHeight.value,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val hue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradient = Brush.linearGradient(
                0.0f to Color.hsv(hue, 0.8f, 0.2f),
                0.5f to Color.hsv((hue + 60) % 360, 0.8f, 0.2f),
                1.0f to Color.hsv((hue + 120) % 360, 0.8f, 0.2f)
            )
            
            drawRect(
                brush = gradient,
                size = size
            )
            
            // رسم دوائر متحركة
            drawCircle(
                color = Color.hsv(hue, 0.5f, 0.3f).copy(alpha = 0.3f),
                radius = size.minDimension / 3,
                center = Offset(circle1X, circle1Y)
            )
            
            drawCircle(
                color = Color.hsv((hue + 180) % 360, 0.5f, 0.3f).copy(alpha = 0.3f),
                radius = size.minDimension / 2.5f,
                center = Offset(circle2X, circle2Y)
            )
            
            // رسم نقاط متلألئة
            for (i in 0..50) {
                val angle = (i * 7.2) * (hue / 180)
                val x = size.width / 2 + cos(angle) * size.minDimension / 4
                val y = size.height / 2 + sin(angle) * size.minDimension / 4
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        
        content()
    }
}

// دالة مساعدة لتحويل HSV إلى Color
fun Color.Companion.hsv(hue: Float, saturation: Float, value: Float): Color {
    val hi = (hue / 60).toInt() % 6
    val f = hue / 60 - hi
    val p = value * (1 - saturation)
    val q = value * (1 - f * saturation)
    val t = value * (1 - (1 - f) * saturation)
    
    val (r, g, b) = when (hi) {
        0 -> Triple(value, t, p)
        1 -> Triple(q, value, p)
        2 -> Triple(p, value, t)
        3 -> Triple(p, q, value)
        4 -> Triple(t, p, value)
        else -> Triple(value, p, q)
    }
    
    return Color(r, g, b)
}

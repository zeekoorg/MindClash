package com.zeeko.mindclash.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.utils.LanguageManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    LaunchedEffect(Unit) {
        delay(2500) // 2.5 ثانية للشاشة الافتتاحية
        onTimeout()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (LanguageManager.isRTL()) "صراع العقول" else "MindClash",
                fontSize = if (isTablet) 64.sp else 48.sp,
                color = Color.White,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "2026",
                fontSize = if (isTablet) 32.sp else 24.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

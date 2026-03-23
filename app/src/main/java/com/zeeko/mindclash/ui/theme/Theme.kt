package com.zeeko.mindclash.ui.theme

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 🌟 نظام الألوان المتطور
private val LuxuryDarkColorScheme = darkColorScheme(
    background = VoidBlack,
    surface = CosmicBlue,
    primary = LiquidGold,
    onPrimary = DiamondWhite,
    secondary = NeonCyan,
    onSecondary = TextSilver,
    tertiary = NeonMagenta,
    error = CrimsonRed,
    onError = DiamondWhite,
    onBackground = TextSilver,
    onSurface = TextSilver,
    surfaceVariant = GlassPurple,
    outline = LiquidGold
)

// ✨ تأثيرات التوهج المتغيرة
@Composable
fun AnimatedThemeBrush(): Brush {
    val infiniteTransition = rememberInfiniteTransition()
    val hue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    return Brush.horizontalGradient(
        colors = listOf(
            Color.hsv(hue, 0.8f, 0.3f),
            Color.hsv((hue + 60) % 360, 0.7f, 0.2f),
            Color.hsv((hue + 120) % 360, 0.8f, 0.3f)
        )
    )
}

@Composable
fun MindClashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val animatedBrush = AnimatedThemeBrush()
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // تأثير شفاف خيالي
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            
            // جعل الخلفية شفافة لتأثير الزجاج
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    MaterialTheme(
        colorScheme = LuxuryDarkColorScheme,
        typography = LuxuryTypography(),
        content = content
    )
}

// 📝 الخطوط الفاخرة
@Composable
fun LuxuryTypography(): Typography {
    return Typography(
        displayLarge = MaterialTheme.typography.displayLarge.copy(
            color = LiquidGold,
            letterSpacing = 0.5.sp
        ),
        displayMedium = MaterialTheme.typography.displayMedium.copy(
            color = TextSilver,
            letterSpacing = 0.3.sp
        ),
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            color = DiamondWhite,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        ),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            color = TextSilver
        ),
        labelLarge = MaterialTheme.typography.labelLarge.copy(
            color = NeonCyan
        )
    )
}

// 🎨 ملحق للـ Color مع تأثيرات إضافية
val Color.glowing: Color
    @Composable get() {
        val infiniteTransition = rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        return this.copy(alpha = alpha)
    }

// 🌟 تأثير تدرج متحرك للخلفيات
@Composable
fun AnimatedBackgroundGradient(): Brush {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    return Brush.horizontalGradient(
        colors = listOf(
            VoidBlack,
            CosmicBlue,
            NebulaBlue,
            VoidBlack
        ),
        startX = offset * 1000f,
        endX = (offset + 0.5f) * 1000f
    )
}

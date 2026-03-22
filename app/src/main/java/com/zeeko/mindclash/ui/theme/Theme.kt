package com.zeeko.mindclash.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// نستخدم فقط الألوان الفاخرة التي عرفناها
private val LuxuryDarkColorScheme = darkColorScheme(
    background = ObsidianBlack,
    surface = MidnightBlue,
    primary = LiquidGold,
    secondary = NeonCyan,
    error = CrimsonRed,
    onBackground = TextSilver,
    onSurface = TextSilver
)

@Composable
fun MindClashTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // جعل شريط الحالة (العلوي) وشريط الأزرار (السفلي) مندمجاً مع خلفية اللعبة
            window.statusBarColor = ObsidianBlack.toArgb()
            window.navigationBarColor = ObsidianBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LuxuryDarkColorScheme,
        content = content
    )
}

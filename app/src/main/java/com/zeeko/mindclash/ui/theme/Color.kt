package com.zeeko.mindclash.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// 🌌 ألوان الكون المظلم
val CosmicBlack = Color(0xFF030014)
val VoidBlack = Color(0xFF010008)
val DarkNebula = Color(0xFF0A0A2A)
val GalacticCore = Color(0xFF1A0F2E)
val Stardust = Color(0xFF2A1A3A)

// 💎 ألوان الزجاج المتوهج
val GlassCrystal = Color(0x33FFFFFF)
val GlassGold = Color(0x33FFD700)
val GlassCyan = Color(0x3300FFFF)
val GlassMagenta = Color(0x33FF00FF)
val GlassPurple = Color(0x33AA00FF)

// 🔥 ألوان النيون السائلة
val LiquidNeonCyan = Color(0xFF00FFFF)
val LiquidNeonMagenta = Color(0xFFFF00FF)
val LiquidNeonGold = Color(0xFFFFD700)
val LiquidNeonRed = Color(0xFFFF2A55)
val LiquidNeonGreen = Color(0xFF39FF14)
val LiquidNeonBlue = Color(0xFF0A66FF)
val LiquidNeonOrange = Color(0xFFFF5F00)
val LiquidNeonPink = Color(0xFFFF10F0)

// 👑 ألوان الذهب الملكي
val RoyalGold = Color(0xFFFFD700)
val ImperialGold = Color(0xFFFFC800)
val AncientGold = Color(0xFFFDB827)
val SparkleGold = Color(0xFFFFF0A3)

// ❤️ ألوان القلب النابض
val HeartBase = Color(0xFFFF2A55)
val HeartGlow = Color(0xFFFF6B6B)
val HeartPulse = Color(0xFFFF8A8A)
val HeartShadow = Color(0xCCFF2A55)

// 🪙 ألوان العملة المتألقة
val CoinBase = Color(0xFFFFD700)
val CoinShine = Color(0xFFFFF0A3)
val CoinShadow = Color(0xFFCCAA33)
val CoinGlow = Color(0x88FFD700)

// ✨ ألوان النصوص المتوهجة
val TextGlow = Color(0xFFFFFFFF)
val TextMystic = Color(0xFFE8E8E8)
val TextDivine = Color(0xFFFFF5E0)

// 🌈 ألوان قوس قزح المتدفق
val RainbowRed = Color(0xFFFF0000)
val RainbowOrange = Color(0xFFFF7F00)
val RainbowYellow = Color(0xFFFFFF00)
val RainbowGreen = Color(0xFF00FF00)
val RainbowBlue = Color(0xFF0000FF)
val RainbowIndigo = Color(0xFF4B0082)
val RainbowViolet = Color(0xFF9400D3)

// ⚡ ألوان الطاقة الكونية
val CosmicEnergy = Color(0xFF7B2F9D)
val AstralBlue = Color(0xFF3A86FF)
val PsionicPurple = Color(0xFF9D4EDD)

// 🎨 تدرجات متحركة (للـ Brush في الكود)
fun cosmicGradient(): Brush = Brush.radialGradient(
    colors = listOf(GalacticCore, VoidBlack, CosmicBlack),
    radius = 1000f
)

fun neonWaveGradient(): Brush = Brush.sweepGradient(
    colors = listOf(
        LiquidNeonCyan,
        LiquidNeonMagenta,
        LiquidNeonGold,
        LiquidNeonCyan
    )
)

fun heartGradient(): Brush = Brush.linearGradient(
    colors = listOf(HeartGlow, HeartBase, HeartShadow)
)

fun coinGradient(): Brush = Brush.radialGradient(
    colors = listOf(CoinShine, CoinBase, CoinShadow),
    radius = 20f
)

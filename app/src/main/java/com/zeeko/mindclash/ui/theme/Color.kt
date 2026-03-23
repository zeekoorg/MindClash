package com.zeeko.mindclash.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// 🌌 الألوان الأساسية
val ObsidianBlack = Color(0xFF0A0A1A)
val VoidBlack = Color(0xFF010008)
val MidnightBlue = Color(0xFF0F172A)
val CosmicBlack = Color(0xFF030014)
val GalacticCore = Color(0xFF1A0F2E)
val CosmicBlue = Color(0xFF1A1F2E)
val NebulaBlue = Color(0xFF16213E)
val DarkNebula = Color(0xFF0A0A2A)

// 💛 الذهب السائل
val LiquidGold = Color(0xFFFFD700)
val RoyalGold = Color(0xFFFFE55C)
val ImperialGold = Color(0xFFFFC800)
val AncientGold = Color(0xFFFDB827)
val SparkleGold = Color(0xFFFFF0A3)

// 💙 النيون السماوي
val NeonCyan = Color(0xFF00FFFF)
val ElectricCyan = Color(0xFF33FFFF)
val LiquidNeonCyan = Color(0xFF00FFFF)
val DeepCyan = Color(0xFF00CCFF)

// ❤️ الأحمر القرمزي
val CrimsonRed = Color(0xFFFF2A55)
val HeartRed = Color(0xFFFF2A55)
val HeartGlow = Color(0xFFFF6B6B)
val HeartPulse = Color(0xFFFF8A8A)
val LiquidNeonRed = Color(0xFFFF2A55)
val BloodRed = Color(0xFFFF1F3F)

// 💚 الأخضر الزمردي
val EmeraldGreen = Color(0xFF2ECC71)
val SuccessGreen = Color(0xFF00FF7F)
val LiquidNeonGreen = Color(0xFF39FF14)

// 💜 الأرجواني السحري
val NeonMagenta = Color(0xFFFF00FF)
val LiquidNeonMagenta = Color(0xFFFF00FF)
val MagicPurple = Color(0xFF9D4EDD)
val CosmicPurple = Color(0xFF7B2F9D)

// 💎 الأبيض والفضي
val TextSilver = Color(0xFFE0E0E0)
val DiamondWhite = Color(0xFFF5F5F5)
val PearlWhite = Color(0xFFF0F0F0)
val TextGlow = Color(0xFFFFFFFF)

// 🪟 تأثيرات الزجاج
val GlassWhite = Color(0x1AFFFFFF)
val GlassCrystal = Color(0x33FFFFFF)
val GlassGold = Color(0x33FFD700)
val GlassCyan = Color(0x3300FFFF)
val GlassMagenta = Color(0x33FF00FF)
val GlassPurple = Color(0x33AA00FF)

// 🪙 العملات
val CoinBase = Color(0xFFFFD700)
val CoinShine = Color(0xFFFFF0A3)
val CoinShadow = Color(0xFFCCAA33)

// 🎨 تدرجات
fun cosmicGradient(): Brush = Brush.radialGradient(
    colors = listOf(GalacticCore, VoidBlack, CosmicBlack),
    radius = 1200f
)

fun nebulaGradient(): Brush = Brush.linearGradient(
    colors = listOf(NebulaBlue, CosmicBlue, DarkNebula)
)

fun neonWaveGradient(): Brush = Brush.sweepGradient(
    colors = listOf(
        LiquidNeonCyan,
        LiquidNeonMagenta,
        LiquidGold,
        LiquidNeonCyan
    )
)

fun heartGradient(): Brush = Brush.linearGradient(
    colors = listOf(HeartGlow, HeartRed, HeartPulse)
)

fun coinGradient(): Brush = Brush.radialGradient(
    colors = listOf(CoinShine, CoinBase, CoinShadow),
    radius = 20f
)

fun goldGradient(): Brush = Brush.horizontalGradient(
    colors = listOf(RoyalGold, AncientGold, RoyalGold)
)

fun successGradient(): Brush = Brush.linearGradient(
    colors = listOf(EmeraldGreen, SuccessGreen)
)

fun errorGradient(): Brush = Brush.linearGradient(
    colors = listOf(CrimsonRed, BloodRed)
)

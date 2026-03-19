package com.zeeko.mindclash.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// أشكال مخصصة للتطبيق
object AppShapes {
    val circle = RoundedCornerShape(50.dp)
    val button = RoundedCornerShape(16.dp)
    val buttonSmall = RoundedCornerShape(8.dp)
    val buttonLarge = RoundedCornerShape(24.dp)
    val buttonRounded = RoundedCornerShape(50.dp)
    val card = RoundedCornerShape(16.dp)
    val cardSmall = RoundedCornerShape(12.dp)
    val cardLarge = RoundedCornerShape(24.dp)
    val cardTop = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val cardBottom = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    val dialog = RoundedCornerShape(24.dp)
    val dialogSmall = RoundedCornerShape(16.dp)
    val letterBox = RoundedCornerShape(12.dp)
    val letterBoxSmall = RoundedCornerShape(8.dp)
    val letterBoxLarge = RoundedCornerShape(16.dp)
    val levelBox = RoundedCornerShape(12.dp)
    val levelBoxSelected = RoundedCornerShape(16.dp)
    val keyboardKey = RoundedCornerShape(50.dp)
    val keyboardKeySpecial = RoundedCornerShape(16.dp)
    val image = RoundedCornerShape(16.dp)
    val imageSmall = RoundedCornerShape(8.dp)
    val imageLarge = RoundedCornerShape(24.dp)
    val imageCircle = RoundedCornerShape(50.dp)
    val sidebar = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    val victoryCard = RoundedCornerShape(24.dp)
    val gradientBackground = RoundedCornerShape(0.dp)
    
    object Tablet {
        val card = RoundedCornerShape(24.dp)
        val button = RoundedCornerShape(20.dp)
        val dialog = RoundedCornerShape(32.dp)
        val letterBox = RoundedCornerShape(16.dp)
    }
}

object AnimatedShapes {
    fun buttonPressed(scale: Float) = RoundedCornerShape(16.dp * scale)
    fun cardScrolled(offset: Float) = RoundedCornerShape(16.dp + offset.dp)
}

fun getShapeForDifficulty(difficulty: Int): RoundedCornerShape {
    return when (difficulty) {
        1 -> AppShapes.cardSmall
        2 -> AppShapes.card
        3 -> AppShapes.cardLarge
        4 -> AppShapes.cardTop
        5 -> AppShapes.cardBottom
        else -> AppShapes.card
    }
}

fun getShapeForLevel(level: Int, isSelected: Boolean): RoundedCornerShape {
    return if (isSelected) {
        when {
            level % 3 == 0 -> AppShapes.levelBoxSelected
            level % 2 == 0 -> AppShapes.card
            else -> AppShapes.cardSmall
        }
    } else {
        AppShapes.levelBox
    }
}

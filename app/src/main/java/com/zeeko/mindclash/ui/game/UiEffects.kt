package com.zeeko.mindclash.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.ui.theme.*
import kotlin.random.Random

@Composable
fun NeoCorrectOverlay() {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1.2f, animationSpec = tween(400, easing = { android.view.animation.OvershootInterpolator(3f).getInterpolation(it) }))
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "✔", fontSize = 120.sp, color = EmeraldGreen, modifier = Modifier.scale(scale.value),
                style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = EmeraldGreen, blurRadius = 40f))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("إجابة أسطورية!", color = EmeraldGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.scale(scale.value))
        }
    }
}

@Composable
fun NeoWrongOverlay() {
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        repeat(5) {
            shakeOffset.animateTo(20f, animationSpec = tween(50))
            shakeOffset.animateTo(-20f, animationSpec = tween(50))
        }
        shakeOffset.animateTo(0f, animationSpec = tween(50))
    }
    Box(modifier = Modifier.fillMaxSize().background(CrimsonRed.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(x = shakeOffset.value.dp)) {
            Text(
                text = "✖", fontSize = 120.sp, color = CrimsonRed,
                style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = CrimsonRed, blurRadius = 40f))
            )
        }
    }
}

@Composable
fun LegendaryResultOverlay(
    title: String, message: String, score: Int, isWin: Boolean,
    buttonText: String, buttonColor: Color, onClick: () -> Unit, onGoHome: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "icon_scale"
    )

    val particles = remember { List(80) { Particle() } }
    val time by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart), label = "time")

    Box(modifier = Modifier.fillMaxSize().background(ObsidianBlack.copy(alpha = 0.85f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
        if (isWin) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    val progress = (time + p.timeOffset) % 1f
                    drawCircle(color = p.color.copy(alpha = 1f - progress), radius = p.size, center = Offset(p.xOffset * size.width, progress * size.height))
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(30.dp)).background(MidnightBlue.copy(alpha = 0.95f))
                .border(2.dp, buttonColor.copy(alpha = 0.6f), RoundedCornerShape(30.dp)).padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = if (isWin) "🏆" else "💀", fontSize = 90.sp, modifier = Modifier.scale(iconScale), style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = buttonColor, blurRadius = 50f)))
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = title, fontSize = 30.sp, fontWeight = FontWeight.Black, color = buttonColor, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = message, fontSize = 18.sp, color = TextSilver, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(25.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = " ⭐ ", fontSize = 28.sp)
                Text(text = score.toString(), fontSize = 55.sp, color = LiquidGold, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(35.dp))
            
            Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = buttonColor.copy(alpha = 0.2f)), border = androidx.compose.foundation.BorderStroke(1.dp, buttonColor), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(55.dp)) {
                Text(text = buttonText, fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(15.dp))
            Button(onClick = onGoHome, colors = ButtonDefaults.buttonColors(containerColor = GlassWhite), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(55.dp)) {
                Text(text = "العودة للخريطة 🏠", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class Particle(val xOffset: Float = Random.nextFloat(), val timeOffset: Float = Random.nextFloat(), val size: Float = Random.nextFloat() * 8f + 4f, val color: Color = listOf(NeonCyan, LiquidGold, EmeraldGreen, CrimsonRed).random())

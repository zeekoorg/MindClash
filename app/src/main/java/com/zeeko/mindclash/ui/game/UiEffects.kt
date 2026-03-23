package com.zeeko.mindclash.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.ui.theme.*

@Composable
fun NeoCorrectOverlay() {
    var scale by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
        delay(800)
        scale = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(EmeraldGreen.copy(alpha = 0.7f), Color.Transparent))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "✓ صحيح! ✓",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
fun NeoWrongOverlay() {
    var scale by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
        delay(800)
        scale = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(CrimsonRed.copy(alpha = 0.7f), Color.Transparent))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "✗ خطأ! ✗",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
fun LegendaryResultOverlay(
    title: String,
    message: String,
    score: Int,
    isWin: Boolean,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit,
    onGoHome: () -> Unit
) {
    var scale by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .scale(scale)
                .background(ObsidianBlack.copy(alpha = 0.95f))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = if (isWin) LiquidGold else CrimsonRed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, fontSize = 18.sp, color = TextSilver)
            Text(text = "نقاطك: $score", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LiquidGold)
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Text(buttonText, color = Color.White)
                }
                Button(
                    onClick = onGoHome,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("🏠 الرئيسية", color = Color.White)
                }
            }
        }
    }
}

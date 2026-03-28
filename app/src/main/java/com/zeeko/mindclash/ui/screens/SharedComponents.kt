package com.zeeko.mindclash.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zeeko.mindclash.ui.theme.VoidBlack

@Composable
fun WinDialog(wonPrizeText: String, wonPrizeIcon: Int, dialogColor: Color, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = { /* إجبار اللاعب على الضغط على موافق */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            
            // خلفية داكنة للتركيز على الجائزة
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)))
            
            // نظام الجزيئات المتساقطة
            FallingParticlesAnimation(iconRes = wonPrizeIcon)

            // صندوق التهنئة
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(30.dp))
                    .background(VoidBlack)
                    .border(3.dp, dialogColor, RoundedCornerShape(30.dp))
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ألف مبروك! 🎉", 
                    fontSize = 36.sp, 
                    fontWeight = FontWeight.Black, 
                    color = dialogColor,
                    style = TextStyle(shadow = Shadow(color = dialogColor, blurRadius = 15f))
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(text = "لقد ربحت:", fontSize = 20.sp, color = Color.White)
                Spacer(modifier = Modifier.height(15.dp))
                
                // أيقونة الجائزة نابضة
                val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
                val iconScale by infiniteTransition.animateFloat(
                    initialValue = 0.9f, targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
                )
                Image(painter = painterResource(id = wonPrizeIcon), contentDescription = null, modifier = Modifier.size(100.dp).scale(iconScale))
                
                Spacer(modifier = Modifier.height(15.dp))
                Text(text = wonPrizeText, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                
                Spacer(modifier = Modifier.height(30.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = dialogColor.copy(alpha = 0.2f)),
                    border = BorderStroke(2.dp, dialogColor),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(55.dp)
                ) {
                    Text("موافق واستلام الجائزة", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class ParticleConfig(val xRatio: Float, val delay: Int, val duration: Int, val size: Dp)

@Composable
fun FallingParticlesAnimation(iconRes: Int) {
    val config = remember { List(25) { ParticleConfig(Math.random().toFloat(), (0..2000).random(), (2500..4500).random(), (20..50).random().dp) } }
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight
        
        config.forEach { c ->
            val infiniteTransition = rememberInfiniteTransition(label = "falling_anim")
            val yOffset by infiniteTransition.animateFloat(
                initialValue = -100f, 
                targetValue = height.value + 100f, 
                animationSpec = infiniteRepeatable(tween(c.duration, delayMillis = c.delay, easing = LinearEasing)), label = "falling_y"
            )
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f, 
                targetValue = 360f, 
                animationSpec = infiniteRepeatable(tween(c.duration, easing = LinearEasing)), label = "falling_rot"
            )
            
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = width * c.xRatio, y = yOffset.dp)
                    .size(c.size)
                    .rotate(rotation)
                    .alpha(0.8f) 
            )
        }
    }
}

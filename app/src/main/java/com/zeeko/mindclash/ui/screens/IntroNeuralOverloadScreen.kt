package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.AudioPlayer
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun IntroOrbitalBreachScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    // --- نظام حماية زر الرجوع (Double Tap to Exit) ---
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            // إذا ضغط مرتين في أقل من ثانيتين، يخرج من التطبيق
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "اضغط مرة أخرى للخروج من صراع العقول", Toast.LENGTH_SHORT).show()
        }
    }

    // --- حالة اللعبة ---
    var currentPhase by remember { mutableIntStateOf(0) } 
    var hits by remember { mutableIntStateOf(0) }
    val maxHits = 3
    var isFiring by remember { mutableStateOf(false) }
    var laserTargetRing by remember { mutableIntStateOf(-1) } 
    
    // زوايا الحلقات
    var angle1 by remember { mutableFloatStateOf(0f) }
    var angle2 by remember { mutableFloatStateOf(0f) }
    var angle3 by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "core")
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = ""
    )
    val shakeAnim = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0f) }

    // محرك الدوران الموزون لضمان وجود فجوات زمنية للاختراق
    LaunchedEffect(currentPhase, hits) {
        if (currentPhase == 2) {
            while (true) {
                withFrameNanos {
                    val baseSpeed = 1.8f + (hits * 0.7f)
                    angle1 = (angle1 + baseSpeed) % 360f          
                    angle2 = (angle2 - (baseSpeed * 0.9f)) % 360f 
                    angle3 = (angle3 + (baseSpeed * 1.5f)) % 360f  
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        AudioPlayer.startHeartbeat()
        delay(2500)
        currentPhase = 1
        AudioPlayer.stopHeartbeat()
        AudioPlayer.playAlarm()
        delay(2000)
        currentPhase = 2
    }

    // دالة قنص النواة
    fun checkBreach() {
        if (isFiring || currentPhase != 2) return
        isFiring = true
        AudioPlayer.playClick()

        val target = 90f // نقطة الإطلاق (الأسفل)
        val gap = 45f    

        fun isBlocked(angle: Float): Boolean {
            val norm = (angle % 360 + 360) % 360
            val diff = abs((norm - target + 540) % 360 - 180)
            return diff > gap / 2f
        }

        scope.launch {
            val r1Blocked = isBlocked(angle1)
            val r2Blocked = isBlocked(angle2)
            val r3Blocked = isBlocked(angle3)

            when {
                r1Blocked -> { laserTargetRing = 1; AudioPlayer.playWrong(); hits = 0; shakeAnim.snapTo(10f); shakeAnim.animateTo(0f) }
                r2Blocked -> { laserTargetRing = 2; AudioPlayer.playWrong(); hits = 0; shakeAnim.snapTo(10f); shakeAnim.animateTo(0f) }
                r3Blocked -> { laserTargetRing = 3; AudioPlayer.playWrong(); hits = 0; shakeAnim.snapTo(10f); shakeAnim.animateTo(0f) }
                else -> {
                    laserTargetRing = 0
                    hits++
                    AudioPlayer.playCorrect()
                    if (hits >= maxHits) {
                        currentPhase = 3
                    }
                }
            }
            delay(350)
            isFiring = false
            laserTargetRing = -1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer { translationX = shakeAnim.value }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { checkBreach() }
    ) {
        // الخلفية
        if (currentPhase > 0) {
            Image(
                painter = painterResource(id = R.drawable.bg_home),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.35f)
            )
        }

        when (currentPhase) {
            1 -> Text(
                text = "⚠️ اختراق مصفوفة الدرع ⚠️", 
                color = CrimsonRed, 
                fontSize = 26.sp, 
                fontWeight = FontWeight.Black, 
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(shadow = Shadow(CrimsonRed, blurRadius = 8f))
            )
            
            2 -> {
                // التقدم
                Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("اكتمال الاختراق: ${hits}/$maxHits", color = LiquidGold, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(maxHits) { i ->
                            Box(modifier = Modifier.size(14.dp).background(if (i < hits) NeonCyan else Color.DarkGray, CircleShape))
                        }
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val gapAngle = 45f
                    val sweep = 360f - gapAngle

                    // دليل التصويب
                    drawLine(Color.White.copy(alpha = 0.08f), Offset(center.x, center.y), Offset(center.x, size.height), 2f)

                    // رسم الليزر
                    if (isFiring) {
                        val endY = when(laserTargetRing) {
                            1 -> center.y + 280f.dp.toPx()
                            2 -> center.y + 210f.dp.toPx()
                            3 -> center.y + 140f.dp.toPx()
                            else -> center.y
                        }
                        drawLine(
                            color = if(laserTargetRing == 0) NeonCyan else Color.Red, 
                            start = Offset(center.x, size.height - 120.dp.toPx()), 
                            end = Offset(center.x, endY), 
                            strokeWidth = 12f, 
                            cap = StrokeCap.Round
                        )
                    }

                    fun drawRing(radius: Float, angle: Float, color: Color) {
                        drawArc(
                            color = color,
                            startAngle = angle + (gapAngle / 2f),
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                    }

                    drawRing(280f.dp.toPx(), angle1, Color.DarkGray)
                    drawRing(210f.dp.toPx(), angle2, Color(0xFF666666))
                    drawRing(140f.dp.toPx(), angle3, Color.LightGray)
                    
                    // المدفع السفلي
                    drawCircle(LiquidGold, 22.dp.toPx(), Offset(size.width / 2, size.height - 120.dp.toPx()))
                }

                // --- النواة (أيقونتك ic_core_nexus) ---
                Box(modifier = Modifier.size(100.dp).align(Alignment.Center).scale(corePulse)) {
                    // توهج خلف الأيقونة
                    Box(modifier = Modifier.fillMaxSize().background(NeonCyan.copy(alpha = 0.15f), CircleShape))
                    Image(
                        painter = painterResource(id = R.drawable.ic_core_nexus),
                        contentDescription = "Core",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            3 -> {
                LaunchedEffect(Unit) {
                    AudioPlayer.playPowerUp()
                    flashAlpha.animateTo(1f, tween(600))
                    delay(1800)
                    sharedPreferences.edit().putBoolean("IntroOrbitalBreachComplete", true).apply()
                    onComplete()
                }
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = flashAlpha.value))) {
                    Text(
                        text = "جيد يمكنك متابعة اللعب...",
                        color = NeonCyan,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}


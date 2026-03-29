package com.zeeko.mindclash.ui.screens

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.AudioPlayer
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun IntroNeuralOverloadScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    // --- مراحل المشهد ---
    var currentPhase by remember { mutableIntStateOf(0) }
    
    // --- حالة اللعبة (Orbital Breach Logic) ---
    var hits by remember { mutableIntStateOf(0) }
    val maxHits = 3
    var isFiring by remember { mutableStateOf(false) }
    var isCoreBroken by remember { mutableStateOf(false) } // لتبديل الأيقونة عند الخطأ أو الفوز
    
    // زوايا الدوران للحلقات
    var angle1 by remember { mutableFloatStateOf(0f) }
    var angle2 by remember { mutableFloatStateOf(120f) }
    var angle3 by remember { mutableFloatStateOf(240f) }

    // --- مؤثرات ---
    val infiniteTransition = rememberInfiniteTransition(label = "global")
    val shakeAnim = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0f) }

    // الدوران المستمر للحلقات في المرحلة 2
    LaunchedEffect(currentPhase, hits) {
        if (currentPhase == 2) {
            while (true) {
                withFrameNanos {
                    val speedBase = 1.2f + (hits * 0.8f) // تزداد السرعة مع كل ضربة
                    angle1 = (angle1 + speedBase) % 360f
                    angle2 = (angle2 - (speedBase * 1.3f)) % 360f
                    angle3 = (angle3 + (speedBase * 1.6f)) % 360f
                }
            }
        }
    }

    // إدارة الأصوات والمراحل
    LaunchedEffect(Unit) {
        AudioPlayer.startHeartbeat()
        delay(3000)
        currentPhase = 1
        AudioPlayer.stopHeartbeat()
        AudioPlayer.playAlarm()
        delay(2500)
        currentPhase = 2
    }

    // دالة التحقق من المحاذاة (هل الفجوة في الأسفل؟)
    fun isAligned(angle: Float): Boolean {
        val gapSize = 50f // حجم الفجوة بالدرجات
        val target = 90f // الزاوية السفلية في الكانفاس
        val normalized = (angle % 360 + 360) % 360
        val diff = abs((normalized - target + 540) % 360 - 180)
        return diff <= gapSize / 2f
    }

    // منطق الإطلاق
    fun fireLaser() {
        if (isFiring || currentPhase != 2) return
        isFiring = true
        AudioPlayer.playClick()

        scope.launch {
            val p1 = isAligned(angle1)
            val p2 = isAligned(angle2)
            val p3 = isAligned(angle3)

            if (p1 && p2 && p3) {
                // نجاح!
                hits++
                AudioPlayer.playCorrect()
                if (hits >= maxHits) {
                    currentPhase = 3
                    isCoreBroken = true // إظهار الأيقونة المحطمة كدليل على الاختراق
                }
            } else {
                // فشل واصطدام
                AudioPlayer.playWrong()
                isCoreBroken = true
                shakeAnim.snapTo(15f)
                shakeAnim.animateTo(0f, spring(Spring.DampingRatioHighBouncy))
                hits = 0 // العودة للصفر (عقاب الألعاب الكبرى)
                delay(200)
                isCoreBroken = false
            }
            delay(400)
            isFiring = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer { translationX = shakeAnim.value }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (currentPhase == 2) fireLaser() }
    ) {
        // الخلفية
        if (currentPhase > 0) {
            Image(
                painter = painterResource(id = R.drawable.bg_home),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(if (currentPhase == 3) 1f else 0.5f)
            )
        }

        when (currentPhase) {
            0 -> Text("جاري تهيئة العصب الذهني...", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            
            1 -> Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚠️ تـحـذيـر ⚠️", color = CrimsonRed, fontSize = 45.sp, fontWeight = FontWeight.Black, style = TextStyle(shadow = Shadow(CrimsonRed, blurRadius = 10f)))
                Text("اختراق الدرع المداري مطلوب للنفاذ", color = Color.White, fontSize = 18.sp, textAlign = TextAlign.Center)
            }

            2 -> {
                // إحصائيات اللعبة في الأعلى
                Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الضربات الناجحة: $hits / $maxHits", color = LiquidGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(maxHits) { i ->
                            Box(modifier = Modifier.size(15.dp).background(if (i < hits) NeonCyan else Color.DarkGray, CircleShape))
                        }
                    }
                }

                // الكانفاس لرسم الدروع والليزر
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val gap = 50f
                    val sweep = 360f - gap

                    // رسم الليزر عند الإطلاق
                    if (isFiring) {
                        drawLine(
                            color = NeonCyan,
                            start = Offset(size.width / 2, size.height - 100.dp.toPx()),
                            end = center,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }

                    // رسم الحلقات الثلاث
                    fun drawOrbital(radius: Float, angle: Float, color: Color) {
                        drawArc(
                            color = color,
                            startAngle = angle + (gap / 2),
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                    }

                    drawOrbital(280f.dp.toPx(), angle1, Color.DarkGray)
                    drawOrbital(210f.dp.toPx(), angle2, Color.Gray)
                    drawOrbital(140f.dp.toPx(), angle3, Color.LightGray)
                    
                    // مطلق الشرارة في الأسفل
                    drawCircle(color = LiquidGold, radius = 15.dp.toPx(), center = Offset(size.width / 2, size.height - 100.dp.toPx()))
                }

                // النواة المركزية (الأيقونة الفخمة)
                Box(modifier = Modifier.size(80.dp).align(Alignment.Center)) {
                    Crossfade(targetState = isCoreBroken, label = "") { broken ->
                        Image(
                            painter = painterResource(id = if (broken) R.drawable.ic_neural_node_broken else R.drawable.ic_neural_node_healthy),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().scale(if (isFiring) 1.2f else 1f)
                        )
                    }
                }
            }

            3 -> {
                LaunchedEffect(Unit) {
                    AudioPlayer.playPowerUp()
                    flashAlpha.animateTo(1f, tween(400))
                    delay(1500)
                    sharedPreferences.edit().putBoolean("IntroOverloadComplete", true).apply()
                    onComplete()
                }
                Text(
                    text = "جيد يمكنك متابعة اللعب...",
                    color = NeonCyan,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).graphicsLayer { alpha = flashAlpha.value }
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = flashAlpha.value)))
            }
        }
    }
}

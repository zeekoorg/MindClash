package com.zeeko.mindclash.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- ✨ 1. تأثير الصح النيون (Native NeoCorrect) ---
@Composable
fun NeoCorrectOverlay() {
    val scale = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // حركة ظهور سريعة مع ارتداد
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = tween(300, easing = { android.view.animation.OvershootInterpolator(4f).getInterpolation(it) })
        )
        // وميض النيون الأخضر
        glowAlpha.animateTo(1f, animationSpec = tween(100))
        glowAlpha.animateTo(0.3f, animationSpec = tween(100))
        glowAlpha.animateTo(1f, animationSpec = tween(100))
    }

    Box(
        modifier = Modifier.fillMaxSize().blur(if (scale.value > 1f) 10.dp else 0.dp), // تأثير ضبابي للخلفية
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // رسم علامة الصح مبرمجة يدوياً (✔)
            Text(
                text = "✔",
                fontSize = 120.sp,
                color = NeonGreen,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale.value).alpha(glowAlpha.value),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(color = NeonGreen, blurRadius = 40f) // توهج قوي
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "إجابة صحيحة!",
                color = NeonGreen,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(scale.value)
            )
        }
    }
}

// --- ✨ 2. تأثير الخطأ المهتز (Native NeoWrong) ---
@Composable
fun NeoWrongOverlay() {
    val scale = remember { Animatable(0f) }
    val shakeOffset = remember { Animatable(0f) } // الأوفست للاهتزاز

    LaunchedEffect(Unit) {
        // ظهور سريع
        scale.animateTo(1f, animationSpec = tween(150))
        // تأثير اهتزاز عنيف (مثل كلمات كراش)
        repeat(5) {
            shakeOffset.animateTo(20f, animationSpec = tween(50))
            shakeOffset.animateTo(-20f, animationSpec = tween(50))
        }
        shakeOffset.animateTo(0f, animationSpec = tween(50))
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), // تعتيم بسيط
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(x = shakeOffset.value.dp) // الاهتزاز الأفقي
        ) {
            Text(
                text = "✖",
                fontSize = 120.sp,
                color = NeonRed,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale.value),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(color = NeonRed, blurRadius = 30f)
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "خطأ!",
                color = NeonRed,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(scale.value)
            )
        }
    }
}

// --- ✨ 3. شاشة الفوز الأسطورية (The Legendary GlassWin) ---
// هذه الشاشة ستحل محل نافذة AlertDialog تماماً!
@Composable
fun LegendaryWinOverlay(score: Int, currentLevel: Int, onNextLevel: () -> Unit, onGoHome: () -> Unit) {
    
    // أنميشن للكأس
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val trophyScale by pulseTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "trophy_scale"
    )

    // أنميشن تساقط القصاصات (برمجة جزيئات - Particle System)
    val particles = remember { List(100) { Particle() } }
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), label = "time")

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        // رسم قصاصات الورق الاحتفالية يدوياً (Canvas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val progress = (time + particle.timeOffset) % 1f
                val y = progress * size.height // تساقط عمودي
                val x = particle.xOffset * size.width // توزيع أفقي

                drawCircle(color = particle.color.copy(alpha = 1f - progress), radius = particle.size, center = androidx.compose.ui.geometry.Offset(x, y))
            }
        }

        // بطاقة الفوز الزجاجية ثلاثية الأبعاد
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF131A2A).copy(alpha = 0.9f))
                .border(2.dp, NeonBlue.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // رسم الكأس النيون اليدوي (🏆)
            Text(
                text = "🏆",
                fontSize = 100.sp,
                modifier = Modifier.scale(trophyScale),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(color = Gold, blurRadius = 50f)
                )
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = "انتصار أسطوري!", fontSize = 36.sp, fontWeight = FontWeight.Black, color = NeonBlue, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "تم تدمير المستوى $currentLevel بنجاح!", fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(25.dp))
            
            // عرض النقاط بشكل بارز جداً
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = " ⭐ ", fontSize = 30.sp)
                Text(text = score.toString(), fontSize = 60.sp, color = Gold, fontWeight = FontWeight.Black)
                Text(text = " نقطة ", fontSize = 24.sp, color = Gold)
            }

            Spacer(modifier = Modifier.height(35.dp))

            // الأزرار الزجاجية المتوهجة
            WinButton(text = "المستوى التالي ➡", color = NeonBlue, onClick = onNextLevel)
            Spacer(modifier = Modifier.height(15.dp))
            WinButton(text = "العودة للخريطة 🏠", color = NeonRed, onClick = onGoHome)
        }
    }
}

@Composable
fun WinButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().height(55.dp)
    ) {
        Text(text = text, fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// كلاس بيانات لقصاصات الورق الاحتفالية
data class Particle(
    val xOffset: Float = Random.nextFloat(),
    val timeOffset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 8f + 4f,
    val color: Color = listOf(NeonBlue, NeonPink, Gold, NeonGreen, NeonRed).random()
)

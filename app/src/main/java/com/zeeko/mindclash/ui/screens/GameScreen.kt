package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.game.GameViewModel
import com.zeeko.mindclash.ui.game.NeoCorrectOverlay
import com.zeeko.mindclash.ui.game.NeoWrongOverlay
import com.zeeko.mindclash.ui.theme.*
import kotlin.random.Random

@Composable
fun GameScreen(
    level: Int,
    adManager: AdManager,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current as Activity

    // تحميل المستوى أول مرة
    LaunchedEffect(Unit) {
        viewModel.loadLevel(level)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- 🎮 الواجهة الأساسية للعبة ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // شريط الإحصائيات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("⭐", state.score.toString(), Gold)
                StatItem("❤️", state.lives.toString(), NeonRed)
                StatItem("⏱️", state.timeLeft.toString(), if (state.timeLeft <= 10) NeonRed else Color.White)
            }

            Spacer(modifier = Modifier.height(30.dp))

            // بطاقة السؤال (Glassmorphism)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.currentQuestion?.question ?: "جاري التحميل...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // مربعات الإجابة تتوهج
            val answerLength = state.currentQuestion?.answer?.length ?: 0
            val borderColor by animateColorAsState(
                targetValue = when {
                    state.showCorrectAnimation -> NeonGreen
                    state.showWrongAnimation -> NeonRed
                    else -> NeonBlue
                },
                animationSpec = tween(durationMillis = 300),
                label = "BorderColor"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0 until answerLength) {
                    val char = state.userAnswer.getOrNull(i)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(45.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = char, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // أزرار المساعدة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HelpButton(icon = "💡", text = "تلميح", color = Gold) {
                    adManager.showRewardedAd(
                        activity = context,
                        onRewardEarned = { Toast.makeText(context, "التلميح: ${state.currentQuestion?.hint}", Toast.LENGTH_LONG).show() },
                        onAdFailed = { Toast.makeText(context, "فشل تحميل الإعلان", Toast.LENGTH_SHORT).show() }
                    )
                }
                HelpButton(icon = "🔍", text = "كشف حرف", color = NeonGreen) {
                    adManager.showRewardedAd(
                        activity = context,
                        onRewardEarned = { viewModel.revealLetter() },
                        onAdFailed = { Toast.makeText(context, "فشل تحميل الإعلان", Toast.LENGTH_SHORT).show() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // الكيبورد الأبجدي المخصص
            NeonKeyboard(
                onLetterClick = { viewModel.onLetterClick(it) },
                onDeleteClick = { viewModel.onDeleteClick() }
            )
        }

        // --- ✨ طبقات الأنميشن المبرمجة محلياً (Native) ---
        AnimatedVisibility(visible = state.showCorrectAnimation, enter = fadeIn(), exit = fadeOut()) {
            NeoCorrectOverlay() // التأثير موجود في ملف UiEffects.kt
        }
        AnimatedVisibility(visible = state.showWrongAnimation, enter = fadeIn(), exit = fadeOut()) {
            NeoWrongOverlay() // التأثير موجود في ملف UiEffects.kt
        }

        // --- 🏆 شاشة الفوز الأسطورية (المستوى اكتمل) ---
        AnimatedVisibility(
            visible = state.isLevelComplete,
            enter = fadeIn() + scaleIn(animationSpec = tween(500)),
            exit = fadeOut() + scaleOut()
        ) {
            LegendaryResultOverlay(
                title = "انتصار أسطوري! 🎉",
                message = "تم تدمير المستوى ${state.currentLevel} بنجاح!",
                score = state.score,
                isWin = true,
                buttonText = "المستوى التالي ➡",
                buttonColor = NeonBlue,
                onClick = {
                    adManager.showInterstitialAd(context) { viewModel.loadLevel(state.currentLevel + 1) }
                },
                onGoHome = {
                    adManager.showInterstitialAd(context) { onNavigateBack() }
                }
            )
        }

        // --- 💀 شاشة الخسارة (انتهت المحاولات) ---
        AnimatedVisibility(
            visible = state.isGameOver,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            LegendaryResultOverlay(
                title = "سقطت العقول! 💀",
                message = "لقد نفذت كل محاولاتك في المستوى ${state.currentLevel}.",
                score = state.score,
                isWin = false,
                buttonText = "إعادة المحاولة 🔄",
                buttonColor = NeonRed,
                onClick = {
                    adManager.showInterstitialAd(context) { viewModel.resetGame() }
                },
                onGoHome = {
                    adManager.showInterstitialAd(context) { onNavigateBack() }
                }
            )
        }
    }
}

// --- 💎 مكونات الشاشة (Components) ---

@Composable
fun StatItem(icon: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 24.sp)
        Text(text = value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HelpButton(icon: String, text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(text = "$icon $text", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun NeonKeyboard(onLetterClick: (Char) -> Unit, onDeleteClick: () -> Unit) {
    val rows = listOf(
        listOf('أ', 'ا', 'ب', 'ت', 'ث', 'ج', 'ح', 'خ'),
        listOf('د', 'ذ', 'ر', 'ز', 'س', 'ش', 'ص', 'ض'),
        listOf('ط', 'ظ', 'ع', 'غ', 'ف', 'ق', 'ك', 'ل'),
        listOf('م', 'ن', 'ه', 'و', 'ي', 'ى', 'ئ', 'ة')
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { char ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { onLetterClick(char) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = char.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onLetterClick(' ') },
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(2f).height(50.dp)
            ) {
                Text("مسافة", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.5f).height(50.dp)
            ) {
                Text("⌫ مسح", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 🎇 شاشة النتيجة الشاملة المبرمجة بالكامل (بدون Lottie) ---
@Composable
fun LegendaryResultOverlay(
    title: String, message: String, score: Int, isWin: Boolean,
    buttonText: String, buttonColor: Color, onClick: () -> Unit, onGoHome: () -> Unit
) {
    // أنميشن الأيقونة (نبض للكأس، واهتزاز للجمجمة)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "icon_scale"
    )

    // أنميشن تساقط القصاصات (للفوز فقط)
    val particles = remember { List(100) { Particle() } }
    val time by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), label = "time")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        // تشغيل نظام الجزيئات (قصاصات الورق) فقط في حالة الفوز
        if (isWin) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { particle ->
                    val progress = (time + particle.timeOffset) % 1f
                    val y = progress * size.height
                    val x = particle.xOffset * size.width
                    drawCircle(color = particle.color.copy(alpha = 1f - progress), radius = particle.size, center = Offset(x, y))
                }
            }
        }

        // بطاقة النتيجة الزجاجية
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF131A2A).copy(alpha = 0.9f))
                .border(2.dp, buttonColor.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isWin) "🏆" else "💀",
                fontSize = 100.sp,
                modifier = Modifier.scale(iconScale),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(color = if (isWin) Gold else NeonRed, blurRadius = 50f)
                )
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = title, fontSize = 32.sp, fontWeight = FontWeight.Black, color = buttonColor, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = message, fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(25.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = " ⭐ ", fontSize = 30.sp)
                Text(text = score.toString(), fontSize = 50.sp, color = Gold, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(35.dp))

            // الأزرار الزجاجية المتوهجة
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, buttonColor),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Text(text = buttonText, fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(15.dp))
            Button(
                onClick = onGoHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Text(text = "العودة للخريطة 🏠", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// كلاس بيانات لقصاصات الورق الاحتفالية المبرمجة
data class Particle(
    val xOffset: Float = Random.nextFloat(),
    val timeOffset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 8f + 4f,
    val color: Color = listOf(NeonBlue, NeonPink, Gold, NeonGreen, NeonRed).random()
)

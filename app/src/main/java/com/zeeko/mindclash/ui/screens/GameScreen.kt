package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.game.GameViewModel
import com.zeeko.mindclash.ui.theme.*

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

            // الكيبورد الأبجدي
            NeonKeyboard(
                onLetterClick = { viewModel.onLetterClick(it) },
                onDeleteClick = { viewModel.onDeleteClick() }
            )
        }

        // --- ✨ طبقة الأنميشن (Lottie) للإجابات ---
        if (state.showCorrectAnimation) {
            LottieOverlay(animationRes = R.raw.correct_anim) // تأكد من وضع الملف في res/raw
        }
        if (state.showWrongAnimation) {
            LottieOverlay(animationRes = R.raw.wrong_anim) // تأكد من وضع الملف في res/raw
        }

        // --- 🏆 شاشة الفوز الأسطورية (Level Complete) ---
        AnimatedVisibility(
            visible = state.isLevelComplete,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            LevelResultOverlay(
                title = "انتصار! 🎉",
                message = "أكملت المستوى بنجاح!",
                score = state.score,
                lottieRes = R.raw.win_anim, // تأكد من وضع الملف في res/raw
                buttonText = "رائع",
                buttonColor = NeonBlue,
                onClick = { adManager.showInterstitialAd(context) { onNavigateBack() } }
            )
        }

        // --- 💀 شاشة الخسارة (Game Over) ---
        AnimatedVisibility(
            visible = state.isGameOver,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            LevelResultOverlay(
                title = "انتهت اللعبة! 💀",
                message = "لقد نفذت محاولاتك.",
                score = state.score,
                lottieRes = null,
                buttonText = "العودة للخريطة",
                buttonColor = NeonRed,
                onClick = { adManager.showInterstitialAd(context) { onNavigateBack() } }
            )
        }
    }
}

// مشغل أنميشن Lottie كطبقة شفافة فوق اللعبة
@Composable
fun LottieOverlay(animationRes: Int) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1 // يعمل مرة واحدة فقط عند الإجابة
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
    }
}

// شاشة النتيجة الفاخرة (زجاجية وتملأ الشاشة)
@Composable
fun LevelResultOverlay(
    title: String,
    message: String,
    score: Int,
    lottieRes: Int?,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)) // تعتيم الخلفية
            .clickable(enabled = false) {}, // لمنع الضغط على اللعبة في الخلف
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF131A2A).copy(alpha = 0.9f))
                .border(2.dp, buttonColor.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // تشغيل أنميشن الاحتفال إذا كان موجوداً
            if (lottieRes != null) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
                val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
                LottieAnimation(composition, { progress }, modifier = Modifier.size(150.dp))
            }

            Text(text = title, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = buttonColor)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = message, fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))
            
            // عرض النقاط بشكل بارز
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 30.dp, vertical = 15.dp)
            ) {
                Text(text = "النقاط: $score ⭐", fontSize = 24.sp, color = Gold, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text(text = buttonText, fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ... (يرجى إبقاء كود StatItem و HelpButton و NeonKeyboard كما هي من الرد السابق لتوفير المساحة) ...


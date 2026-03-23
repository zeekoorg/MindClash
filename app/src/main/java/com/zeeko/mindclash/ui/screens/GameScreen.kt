package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.game.GameViewModel
import com.zeeko.mindclash.ui.game.NeoCorrectOverlay
import com.zeeko.mindclash.ui.game.NeoWrongOverlay
import com.zeeko.mindclash.ui.game.LegendaryResultOverlay
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
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.loadLevel(level)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(ObsidianBlack, MidnightBlue)))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- 🌟 الجزء العلوي: الإحصائيات ولوجو اللعبة الكوني ---
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                
                // لوجو صراع العقول في الوسط (ذهبي مزخرف بدون خلفية)
                Text(
                    text = "صراع العقول",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = LiquidGold,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(color = LiquidGold, blurRadius = 20f)
                    ),
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // الإحصائيات (يسار ويمين)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItemGlassXml(R.drawable.ic_heart_glass, state.lives.toString(), CrimsonRed)
                    StatItemGlassXml(R.drawable.ic_star_glass, state.score.toString(), LiquidGold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 💡 التلميح الدائم (Glass Hint) ---
            AnimatedVisibility(visible = state.isHintVisible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp).clip(RoundedCornerShape(15.dp)).background(LiquidGold.copy(alpha = 0.15f)).border(1.dp, LiquidGold, RoundedCornerShape(15.dp)).padding(15.dp), contentAlignment = Alignment.Center) {
                    Text(text = "💡 تلميح: ${state.currentQuestion?.hint ?: ""}", color = LiquidGold, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }

            // --- 🃏 بطاقة السؤال الزجاجية ---
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(GlassWhite).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)).padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.currentQuestion?.question ?: "جاري تهيئة العقول...", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, lineHeight = 36.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- 🔠 مربعات الإجابة النيون ---
            val borderColor by animateColorAsState(
                targetValue = when { state.showCorrectAnimation -> EmeraldGreen; state.showWrongAnimation -> CrimsonRed; else -> NeonCyan },
                animationSpec = tween(300), label = "AnswerBorderColor"
            )
            
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.Center) {
                val answerLength = state.currentQuestion?.answer?.length ?: 0
                for (i in 0 until answerLength) {
                    val char = state.userAnswer.getOrNull(i)?.toString() ?: ""
                    Box(modifier = Modifier.padding(horizontal = 4.dp).size(50.dp).clip(RoundedCornerShape(12.dp)).background(GlassWhite).border(2.dp, borderColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text(text = char, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- 🆘 أزرار المساعدة وربط الإعلانات الذكي ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                HelpButton(icon = "💡", text = "كشف التلميح", color = LiquidGold) {
                    if (!state.isHintVisible) {
                        adManager.showRewardedAd(activity = context, onRewardEarned = { viewModel.showPermanentHint() }, onAdFailed = { Toast.makeText(context, "الإعلان غير جاهز، جرب بعد قليل", Toast.LENGTH_SHORT).show() })
                    }
                }
                HelpButton(icon = "🔍", text = "كشف حرف", color = NeonCyan) {
                    adManager.showRewardedAd(activity = context, onRewardEarned = { viewModel.revealLetter() }, onAdFailed = { Toast.makeText(context, "الإعلان غير جاهز، جرب بعد قليل", Toast.LENGTH_SHORT).show() })
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- ⌨️ لوحة المفاتيح والاهتزاز اللمسي مع ترتيبك الجديد ---
            NeonKeyboard(
                onLetterClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onLetterClick(it)
                },
                onDeleteClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onDeleteClick()
                }
            )
        }

        // --- ✨ طبقات الأنميشن والمؤثرات ---
        AnimatedVisibility(visible = state.showCorrectAnimation, enter = fadeIn(), exit = fadeOut()) { NeoCorrectOverlay() }
        AnimatedVisibility(visible = state.showWrongAnimation, enter = fadeIn(), exit = fadeOut()) { NeoWrongOverlay() }

        // --- 🏆 شاشة الفوز ---
        AnimatedVisibility(visible = state.isLevelComplete, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
            LegendaryResultOverlay(
                title = "انتصار كاسح! 🎉", message = "تم تدمير أسرار المستوى ${state.currentLevel}!", score = state.score, isWin = true, buttonText = "اقتحم المستوى التالي ➡", buttonColor = NeonCyan,
                onClick = { adManager.showInterstitialAd(context) { viewModel.loadLevel(state.currentLevel + 1) } },
                onGoHome = { adManager.showInterstitialAd(context) { onNavigateBack() } }
            )
        }

        // --- 💀 شاشة الخسارة ---
        AnimatedVisibility(visible = state.isGameOver, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
            LegendaryResultOverlay(
                title = "سقطت العقول! 💀", message = "تم تدمير كل دفاعاتك في المستوى ${state.currentLevel}", score = state.score, isWin = false, buttonText = "انتقام (إعادة) 🔄", buttonColor = CrimsonRed,
                onClick = { adManager.showInterstitialAd(context) { viewModel.resetGame() } },
                onGoHome = { adManager.showInterstitialAd(context) { onNavigateBack() } }
            )
        }
    }
}

// --- 💎 المكونات المصغرة ---

// مكون الإحصائيات الزجاجي المضيء الجديد
@Composable
fun StatItemGlassXml(iconRes: Int, value: String, color: Color) {
    Box(
        modifier = Modifier.size(height = 60.dp, width = 110.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = value,
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 10.dp) // لضمان التمركز مع الرمز
        )
    }
}

@Composable
fun HelpButton(icon = "💡", text: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f)), border = androidx.compose.foundation.BorderStroke(1.dp, color), shape = RoundedCornerShape(20.dp)) {
        Text(text = "$icon $text", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun NeonKeyboard(onLetterClick: (Char) -> Unit, onDeleteClick: () -> Unit) {
    var isNumeric by remember { mutableStateOf(false) }

    // الترتيب الأبجدي المزدوج الشامل (9 حروف في السطر)
    val letterRows = listOf(
        listOf('ج', 'ح', 'خ', 'ه', 'ع', 'غ', 'ف', 'ق', 'ص'),
        listOf('ض', 'ك', 'م', 'ن', 'ت', 'ا', 'ل', 'ب', 'ي'),
        listOf('س', 'ش', 'ء', 'ث', 'ة', 'و', 'ر', 'ز', 'د'),
        listOf('ذ', 'ط', 'ظ', 'ى', 'ئ', 'ؤ', 'إ', 'أ', 'آ') 
    )

    val numberRows = listOf(
        listOf('3', '2', '1'),
        listOf('6', '5', '4'),
        listOf('9', '8', '7')
    )

    val activeRows = if (isNumeric) numberRows else letterRows

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        
        // الأسطر الأساسية للكيبورد
        activeRows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { char ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(GlassWhite).clickable { onLetterClick(char) }, contentAlignment = Alignment.Center) {
                        Text(text = char.toString(), color = TextSilver, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- تصحيح زر الـ 0 والترتيب السفلي (الأرقام، المسافة، المسح) ---
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            
            // 🚀 زر تبديل الأرقام في أقصى اليمين (يُكتب الكود أولاً ليظهر يميناً في النظام العربي)
            Button(
                onClick = { isNumeric = !isNumeric },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(55.dp)
            ) {
                Text(if (isNumeric) "أبج" else "123", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // زر المسافة في الوسط
            Button(
                onClick = { onLetterClick(' ') },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(2f).height(55.dp)
            ) {
                Text("مسافة", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // 🚀 زر المسح والـ 0 في أقصى اليسار (تبديل ذكي)
            if (isNumeric) {
                // إذا كنا في لوحة الأرقام، نظهر الـ 0 مربعاً والمسح بجانبه
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // زر 0 مربع
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(GlassWhite).clickable { onLetterClick('0') }, contentAlignment = Alignment.Center) {
                        Text(text = "0", color = TextSilver, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    // زر المسح مربع
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(CrimsonRed.copy(alpha = 0.2f)).clickable { onDeleteClick() }, contentAlignment = Alignment.Center) {
                        Text(text = "⌫", color = CrimsonRed, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // إذا كنا في لوحة الحروف، نظهر المسح مستطيلاً
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(55.dp)
                ) {
                    Text("⌫ مسح", color = CrimsonRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

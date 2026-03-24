package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

@Composable
fun GameScreen(
    level: Int,
    adManager: AdManager,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current as Activity
    
    // للتحكم بكيبورد الهاتف الأساسي
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // للتحكم بنوافذ الإعلانات والعملات
    var showAdDialog by remember { mutableStateOf(false) }
    var adDialogType by remember { mutableStateOf("") } // "hint", "letter", أو "coins"

    LaunchedEffect(Unit) {
        viewModel.loadLevel(level)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // 1. الخلفية الرئيسية للعبة (صورتك)
        Image(
            painter = painterResource(id = R.drawable.bg_game),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- 🌟 الجزء العلوي: القلوب (يمين)، اللوجو (وسط)، العملات (يسار) ---
            // نستخدم الترتيب العربي (اليمين أولاً)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // القلوب (أعلى اليمين)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_heart_custom),
                        contentDescription = "Hearts",
                        modifier = Modifier.size(60.dp)
                    )
                    Text(text = state.lives.toString(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                // لوجو اللعبة (في المنتصف)
                Image(
                    painter = painterResource(id = R.drawable.logo_game),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(80.dp).weight(1f).padding(horizontal = 10.dp)
                )

                // العملات (أعلى اليسار) - قابلة للضغط
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) {
                        adDialogType = "coins"
                        showAdDialog = true
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_coin_custom),
                        contentDescription = "Coins",
                        modifier = Modifier.size(60.dp)
                    )
                    Text(text = state.score.toString(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 🃏 حاوية السؤال (مستطيل بخلفيتك الخاصة) ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_question),
                    contentDescription = "Question Container",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = state.currentQuestion?.question ?: "جاري تهيئة العقول...",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- ⌨️ نظام كيبورد الهاتف (استقبال النص بالمربعات) ---
            BasicTextField(
                value = state.userAnswer,
                onValueChange = { viewModel.onNativeKeyboardInput(it) },
                modifier = Modifier.size(1.dp).alpha(0f).focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // مربعات الإجابة (عند النقر يفتح الكيبورد)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
                horizontalArrangement = Arrangement.Center
            ) {
                val answerLength = state.currentQuestion?.answer?.length ?: 0
                for (i in 0 until answerLength) {
                    val char = state.userAnswer.getOrNull(i)?.toString() ?: ""
                    Box(
                        modifier = Modifier.padding(horizontal = 4.dp).size(55.dp).clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(2.dp, if (char.isNotEmpty()) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = char, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- 🆘 أزرار المساعدة السفلية ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { if (state.score >= 50) viewModel.buyHint() else { adDialogType = "hint"; showAdDialog = true } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)), shape = RoundedCornerShape(15.dp)
                ) {
                    Text("💡 تلميح", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Button(
                    onClick = { if (state.score >= 50) viewModel.buyRevealLetter() else { adDialogType = "letter"; showAdDialog = true } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF)), shape = RoundedCornerShape(15.dp)
                ) {
                    Text("🔍 كشف حرف", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- 📺 نافذة الدايلوج الاحترافي ---
        if (showAdDialog) {
            val dialogTitle = if (adDialogType == "coins") "عملات مجانية!" else "رصيد غير كافٍ!"
            val dialogMessage = when (adDialogType) {
                "hint" -> "لقد نفذت العملات الذهبية، هل تريد مشاهدة إعلان لكشف تلميح؟"
                "letter" -> "لقد نفذت العملات الذهبية، هل تريد مشاهدة إعلان لكشف حرف؟"
                else -> "هل تريد مشاهدة إعلان للحصول على 50 عملة ذهبية؟"
            }
            
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(30.dp)).background(Color(0xFF0F172A)).border(2.dp, Color(0xFFFFD700), RoundedCornerShape(30.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🪙", fontSize = 70.sp)
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = dialogTitle, fontSize = 28.sp, fontWeight = FontWeight.Black, color = if(adDialogType == "coins") Color(0xFFFFD700) else Color(0xFFFF2A55))
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = dialogMessage, fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    Button(
                        onClick = {
                            showAdDialog = false
                            adManager.showRewardedAd(
                                activity = context,
                                onRewardEarned = { 
                                    when (adDialogType) {
                                        "hint" -> viewModel.showPermanentHint()
                                        "letter" -> viewModel.revealLetterFree()
                                        "coins" -> viewModel.rewardCoins(50)
                                    }
                                },
                                onAdFailed = { Toast.makeText(context, "الإعلان غير جاهز", Toast.LENGTH_SHORT).show() }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)), modifier = Modifier.fillMaxWidth().height(55.dp)
                    ) { Text("مشاهدة إعلان 📺", fontSize = 18.sp, color = Color.White) }
                    Spacer(modifier = Modifier.height(15.dp))
                    Button(onClick = { showAdDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray), modifier = Modifier.fillMaxWidth().height(55.dp)) { Text("إلغاء", fontSize = 18.sp, color = Color.White) }
                }
            }
        }
        
        // الأنميشن القديم (يمكنك استبداله بصورك لاحقاً)
        AnimatedVisibility(visible = state.showCorrectAnimation, enter = fadeIn(), exit = fadeOut()) { NeoCorrectOverlay() }
        AnimatedVisibility(visible = state.showWrongAnimation, enter = fadeIn(), exit = fadeOut()) { NeoWrongOverlay() }
        AnimatedVisibility(visible = state.isLevelComplete, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) { LegendaryResultOverlay(title = "انتصار! 🎉", message = "تم تدمير أسرار المستوى", score = state.score, isWin = true, buttonText = "التالي ➡", buttonColor = Color(0xFF00E5FF), onClick = { adManager.showInterstitialAd(context) { viewModel.loadLevel(state.currentLevel + 1) } }, onGoHome = { adManager.showInterstitialAd(context) { onNavigateBack() } }) }
        AnimatedVisibility(visible = state.isGameOver, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) { LegendaryResultOverlay(title = "سقطت العقول! 💀", message = "حاول مرة أخرى", score = state.score, isWin = false, buttonText = "إعادة 🔄", buttonColor = Color(0xFFFF2A55), onClick = { adManager.showInterstitialAd(context) { viewModel.resetGame() } }, onGoHome = { adManager.showInterstitialAd(context) { onNavigateBack() } }) }
    }
}

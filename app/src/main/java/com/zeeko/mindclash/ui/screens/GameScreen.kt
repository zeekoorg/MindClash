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
    
    // للتحكم بكيبورد الهاتف الأساسي
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // للتحكم بنوافذ الإعلانات
    var showAdDialog by remember { mutableStateOf(false) }
    var adDialogType by remember { mutableStateOf("") } // "hint", "letter", أو "coins"

    LaunchedEffect(Unit) {
        viewModel.loadLevel(level)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // 1. الخلفية الرئيسية للعبة (صورتك المصممة)
        Image(
            painter = painterResource(id = R.drawable.bg_game),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- 🌟 الجزء العلوي: العملات (يسار)، اللوجو (وسط)، القلوب (يمين) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // العملات (أعلى اليسار)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // فتح نافذة عرض إعلان لزيادة العملات
                        adDialogType = "coins"
                        showAdDialog = true
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_coin_custom),
                        contentDescription = "Coins",
                        modifier = Modifier.size(50.dp)
                    )
                    Text(text = state.score.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                // لوجو اللعبة (المنتصف)
                Image(
                    painter = painterResource(id = R.drawable.logo_game),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(80.dp)
                        .weight(1f) // يأخذ المساحة المتبقية في المنتصف
                        .padding(horizontal = 10.dp)
                )

                // القلوب (أعلى اليمين)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_heart_custom),
                        contentDescription = "Hearts",
                        modifier = Modifier.size(50.dp)
                    )
                    Text(text = state.lives.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- 💡 التلميح (يظهر عند الشراء) ---
            AnimatedVisibility(visible = state.isHintVisible) {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp).clip(RoundedCornerShape(15.dp)).background(Color.Black.copy(alpha = 0.5f)).padding(15.dp), contentAlignment = Alignment.Center) {
                    Text(text = "💡 تلميح: ${state.currentQuestion?.hint ?: ""}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // --- 🃏 حاوية السؤال (صورتك المصممة) ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // يمكنك تعديل الارتفاع حسب صورتك
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_question),
                    contentDescription = "Question Container",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = state.currentQuestion?.question ?: "جاري تهيئة العقول...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- ⌨️ نظام كيبورد الهاتف (المربعات المخفية والظاهرة) ---
            
            // الحقل المخفي الذي يستدعي كيبورد الهاتف ويستقبل النص
            BasicTextField(
                value = state.userAnswer,
                onValueChange = { viewModel.onNativeKeyboardInput(it) },
                modifier = Modifier
                    .size(1.dp)
                    .alpha(0f)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // المربعات الظاهرة للمستخدم (عند النقر عليها يفتح الكيبورد)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                val answerLength = state.currentQuestion?.answer?.length ?: 0
                for (i in 0 until answerLength) {
                    val char = state.userAnswer.getOrNull(i)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(55.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(2.dp, if (char.isNotEmpty()) NeonCyan else Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = char, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            
            // نص صغير يرشد المستخدم
            Text(
                text = "انقر على المربعات للكتابة",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 10.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- 🆘 أزرار المساعدة السفلية ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { if (!state.isHintVisible) { if (state.score >= 50) viewModel.buyHint() else { adDialogType = "hint"; showAdDialog = true } } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)), border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGold), shape = RoundedCornerShape(15.dp)
                ) {
                    Text("💡 تلميح", color = LiquidGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Button(
                    onClick = { if (state.score >= 50) viewModel.buyRevealLetter() else { adDialogType = "letter"; showAdDialog = true } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)), border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan), shape = RoundedCornerShape(15.dp)
                ) {
                    Text("🔍 حرف", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- 📺 نافذة الدايلوج الاحترافي (عند نفاذ العملات أو طلب زيادة) ---
        if (showAdDialog) {
            val dialogTitle = if (adDialogType == "coins") "عملات مجانية!" else "رصيد غير كافٍ!"
            val dialogMessage = when (adDialogType) {
                "hint" -> "لقد نفذت العملات الذهبية، هل تريد مشاهدة إعلان لكشف تلميح؟"
                "letter" -> "لقد نفذت العملات الذهبية، هل تريد مشاهدة إعلان لكشف حرف؟"
                else -> "هل تريد مشاهدة إعلان للحصول على 50 عملة ذهبية؟"
            }
            
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(30.dp)).background(MidnightBlue.copy(alpha = 0.95f)).border(2.dp, LiquidGold, RoundedCornerShape(30.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🪙", fontSize = 70.sp)
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = dialogTitle, fontSize = 28.sp, fontWeight = FontWeight.Black, color = if(adDialogType == "coins") LiquidGold else CrimsonRed)
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = dialogMessage, fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center, lineHeight = 28.sp)
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
                                onAdFailed = { Toast.makeText(context, "الإعلان غير جاهز حالياً", Toast.LENGTH_SHORT).show() }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidGold.copy(alpha = 0.2f)), border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGold), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(55.dp)
                    ) {
                        Text("مشاهدة إعلان 📺", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Button(onClick = { showAdDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = GlassWhite), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(55.dp)) {
                        Text("إلغاء", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        AnimatedVisibility(visible = state.showCorrectAnimation, enter = fadeIn(), exit = fadeOut()) { NeoCorrectOverlay() }
        AnimatedVisibility(visible = state.showWrongAnimation, enter = fadeIn(), exit = fadeOut()) { NeoWrongOverlay() }
        AnimatedVisibility(visible = state.isLevelComplete, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) { LegendaryResultOverlay(title = "انتصار كاسح! 🎉", message = "تم تدمير أسرار المستوى ${state.currentLevel}!", score = state.score, isWin = true, buttonText = "اقتحم المستوى التالي ➡", buttonColor = NeonCyan, onClick = { adManager.showInterstitialAd(context) { viewModel.loadLevel(state.currentLevel + 1) } }, onGoHome = { adManager.showInterstitialAd(context) { onNavigateBack() } }) }
        AnimatedVisibility(visible = state.isGameOver, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) { LegendaryResultOverlay(title = "سقطت العقول! 💀", message = "تم تدمير كل دفاعاتك في المستوى ${state.currentLevel}", score = state.score, isWin = false, buttonText = "انتقام (إعادة) 🔄", buttonColor = CrimsonRed, onClick = { adManager.showInterstitialAd(context) { viewModel.resetGame() } }, onGoHome = { adManager.showInterstitialAd(context) { onNavigateBack() } }) }
    }
}


package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.game.GameViewModel
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(
    level: Int,
    adManager: AdManager,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current as Activity
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var showAdDialog by remember { mutableStateOf(false) }
    var adDialogType by remember { mutableStateOf("") } 

    // --- 🔑 الحل السحري لمشكلة المؤشر (Cursor) ---
    var textFieldValue by remember { mutableStateOf(TextFieldValue(state.userAnswer)) }

    // هذا الكود يراقب أي تحديث للإجابة (مثل كشف حرف) ويجبر المؤشر على الذهاب للنهاية
    LaunchedEffect(state.userAnswer) {
        if (textFieldValue.text != state.userAnswer) {
            textFieldValue = TextFieldValue(
                text = state.userAnswer,
                selection = TextRange(state.userAnswer.length) // إجبار المؤشر للبقاء في النهاية
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadLevel(level)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        Image(
            painter = painterResource(id = R.drawable.bg_game),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // العملات والقلوب واللوجو
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) { adDialogType = "coins"; showAdDialog = true }
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_coin_custom), contentDescription = "Coins", modifier = Modifier.size(60.dp))
                    Text(text = state.score.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 10f)))
                }

                Image(painter = painterResource(id = R.drawable.logo_game), contentDescription = "Logo", contentScale = ContentScale.Fit, modifier = Modifier.height(80.dp).weight(1f).padding(horizontal = 10.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) { adDialogType = "hearts"; showAdDialog = true }
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_heart_custom), contentDescription = "Hearts", modifier = Modifier.size(60.dp))
                    Text(text = state.lives.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, style = TextStyle(shadow = Shadow(color = CrimsonRed, blurRadius = 10f)))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // حاوية السؤال
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            ) {
                Image(painter = painterResource(id = R.drawable.bg_question), contentDescription = "Question", contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
                Text(text = state.currentQuestion?.question ?: "جاري تهيئة العقول...", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, lineHeight = 36.sp, modifier = Modifier.padding(25.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // كود ظهور التلميح
            AnimatedVisibility(
                visible = state.isHintVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = " تلميح: ${state.currentQuestion?.hint ?: "لا يوجد تلميح"}",
                    color = LiquidGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
            }

            // التعرف الذكي على لغة الإجابة لتعديل اتجاه المربعات
            val currentAnswer = state.currentQuestion?.answer ?: ""
            val isArabicAnswer = currentAnswer.any { it in '\u0600'..'\u06FF' }
            val layoutDirection = if (isArabicAnswer) LayoutDirection.Rtl else LayoutDirection.Ltr

            // حقل النص المحدث لحل مشكلة المؤشر
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    val answerLength = state.currentQuestion?.answer?.length ?: 0
                    val cleanText = newValue.text.replace("\n", "") // منع نزول السطر
                    if (cleanText.length <= answerLength) {
                        textFieldValue = newValue.copy(text = cleanText)
                        viewModel.onNativeKeyboardInput(cleanText)
                    }
                },
                modifier = Modifier.size(1.dp).alpha(0f).focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // تطبيق الاتجاه الذكي على المربعات
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val answerLength = currentAnswer.length
                    for (i in 0 until answerLength) {
                        val char = state.userAnswer.getOrNull(i)?.toString() ?: ""
                        val borderColor = if (char.isNotEmpty()) NeonCyan else Color.White.copy(alpha = 0.5f)
                        
                        Box(
                            modifier = Modifier.padding(horizontal = 4.dp).size(55.dp).clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black,
                                style = TextStyle(shadow = Shadow(color = borderColor, blurRadius = 8f))
                            )
                        }
                    }
                }
            }
            
            Text(
                text = "انقر على المربعات للكتابة", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp,
                modifier = Modifier.padding(top = 10.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // أزرار المساعدة السفلية
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { if (state.score >= 50) viewModel.buyHint() else { adDialogType = "hint"; showAdDialog = true } }
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_btn_hint), contentDescription = "Hint", modifier = Modifier.size(70.dp).padding(5.dp))
                    Text("تلميح", color = LiquidGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { if (state.score >= 50) viewModel.buyRevealLetter() else { adDialogType = "letter"; showAdDialog = true } }
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_btn_reveal), contentDescription = "Reveal", modifier = Modifier.size(70.dp).padding(5.dp))
                    Text("كشف حرف", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // نافذة الإعلانات
        if (showAdDialog) {
            val dialogTitle = when (adDialogType) { "coins" -> "عملات مجانية!"; "hearts" -> "قلوب إضافية!"; else -> "رصيد غير كافٍ!" }
            val dialogMessage = when (adDialogType) {
                "hint" -> "لقد نفذت العملات الذهبية، هل تريد مشاهدة إعلان لكشف تلميح؟"
                "letter" -> "لقد نفذت العملات الذهبية، هل تريد مشاهدة إعلان لكشف حرف؟"
                "hearts" -> "هل تريد مشاهدة إعلان للحصول على 5 قلوب إضافية لتستمر باللعب؟"
                else -> "هل تريد مشاهدة إعلان للحصول على 50 عملة ذهبية؟"
            }
            val mainColor = if(adDialogType == "hearts") CrimsonRed else LiquidGold
            
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(30.dp)).background(VoidBlack).border(2.dp, mainColor, RoundedCornerShape(30.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(id = R.drawable.ic_watch_ad), contentDescription = "Ad Icon", modifier = Modifier.size(100.dp))
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = dialogTitle, fontSize = 28.sp, fontWeight = FontWeight.Black, color = mainColor)
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = dialogMessage, fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center, lineHeight = 28.sp)
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(onClick = {
                        showAdDialog = false
                        adManager.showRewardedAd(
                            activity = context,
                            onRewardEarned = { 
                                when (adDialogType) {
                                    "hint" -> viewModel.showPermanentHint()
                                    "letter" -> viewModel.revealLetterFree()
                                    "coins" -> viewModel.rewardCoins(50)
                                    "hearts" -> viewModel.rewardLives(5)
                                }
                            },
                            onAdFailed = { Toast.makeText(context, "الإعلان غير جاهز حالياً", Toast.LENGTH_SHORT).show() }
                        )
                    }, colors = ButtonDefaults.buttonColors(containerColor = mainColor.copy(alpha = 0.2f)), border = androidx.compose.foundation.BorderStroke(1.dp, mainColor), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(55.dp)) {
                        Text("مشاهدة إعلان ", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Button(onClick = { showAdDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(55.dp)) {
                        Text("إلغاء", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // المؤثرات
        AnimatedVisibility(visible = state.showCorrectAnimation, enter = fadeIn(), exit = fadeOut()) { NeoCorrectOverlayCustom() }
        AnimatedVisibility(visible = state.showWrongAnimation, enter = fadeIn(), exit = fadeOut()) { NeoWrongOverlayCustom() }
        AnimatedVisibility(visible = state.isLevelComplete, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) { 
            LegendaryResultOverlayCustom(title = "انتصار كاسح! 🎉", message = "تم تدمير أسرار المستوى ${state.currentLevel}!", score = state.score, isWin = true, buttonText = "اقتحم المستوى التالي ", buttonColor = NeonCyan, onClick = { adManager.showInterstitialAd(context) { viewModel.loadLevel(state.currentLevel + 1) } }, onGoHome = { adManager.showInterstitialAd(context) { onNavigateBack() } }) 
        }
        AnimatedVisibility(visible = state.isGameOver, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) { 
            LegendaryResultOverlayCustom(title = "سقطت العقول! 💀", message = "تم تدمير كل دفاعاتك في المستوى ${state.currentLevel}", score = state.score, isWin = false, buttonText = "إعادة", buttonColor = CrimsonRed, onClick = { adManager.showInterstitialAd(context) { viewModel.resetGame() } }, onGoHome = { adManager.showInterstitialAd(context) { onNavigateBack() } }) 
        }
    }
}

// مكونات المؤثرات السفلية
@Composable
fun NeoCorrectOverlayCustom() {
    var scale by remember { mutableStateOf(0f) }
    var glowAlpha by remember { mutableStateOf(0.4f) }
    val infiniteTransition = rememberInfiniteTransition(label = "")
    glowAlpha = infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 0.8f, animationSpec = infiniteRepeatable(animation = tween(400, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "").value
    LaunchedEffect(Unit) { scale = 1.2f; delay(600); scale = 0f }
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF00FF7F).copy(alpha = 0.4f * glowAlpha)).alpha(scale), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = R.drawable.ic_status_correct), contentDescription = "Correct", modifier = Modifier.fillMaxWidth(0.6f).aspectRatio(1f).scale(scale))
    }
}

@Composable
fun NeoWrongOverlayCustom() {
    var scale by remember { mutableStateOf(0f) }
    var shake by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) { scale = 1.2f; shake = 15f; delay(100); shake = -15f; delay(100); shake = 10f; delay(100); shake = 0f; delay(400); scale = 0f }
    Box(modifier = Modifier.fillMaxSize().background(CrimsonRed.copy(alpha = 0.4f)).alpha(scale), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = R.drawable.ic_status_wrong), contentDescription = "Wrong", modifier = Modifier.fillMaxWidth(0.6f).aspectRatio(1f).scale(scale).then(Modifier.offset(x = shake.dp)))
    }
}

@Composable
fun LegendaryResultOverlayCustom(title: String, message: String, score: Int, isWin: Boolean, buttonText: String, buttonColor: Color, onClick: () -> Unit, onGoHome: () -> Unit) {
    var scale by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) { scale = 1f }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxWidth(0.9f).scale(scale).clip(RoundedCornerShape(40.dp)).background(VoidBlack).border(2.dp, if(isWin) LiquidGold else CrimsonRed, RoundedCornerShape(40.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = if(isWin) R.drawable.ic_state_win else R.drawable.ic_state_gameover), contentDescription = "Result", modifier = Modifier.fillMaxWidth(0.6f).aspectRatio(1f))
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = title, fontSize = 34.sp, fontWeight = FontWeight.Black, color = if(isWin) LiquidGold else CrimsonRed, textAlign = TextAlign.Center, style = TextStyle(shadow = Shadow(color = if(isWin) LiquidGold else CrimsonRed, blurRadius = 15f)))
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = message, fontSize = 20.sp, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "نقاط المستوى: $score", fontSize = 26.sp, fontWeight = FontWeight.Black, color = LiquidGold, style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 10f)))
            Spacer(modifier = Modifier.height(30.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = buttonColor.copy(alpha = 0.2f)), border = androidx.compose.foundation.BorderStroke(1.dp, buttonColor), modifier = Modifier.weight(1f).height(55.dp), shape = RoundedCornerShape(20.dp)) { Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                Button(onClick = onGoHome, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray), modifier = Modifier.weight(1f).height(55.dp), shape = RoundedCornerShape(20.dp)) { Text("🏠 الرئيسية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
        }
    }
}

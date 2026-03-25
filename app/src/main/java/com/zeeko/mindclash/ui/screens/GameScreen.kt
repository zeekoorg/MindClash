package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.game.GameViewModel
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay

// ==================== الشاشة الرئيسية للعب (استقبال النصوص، اقتصاد العملات، الكيبورد) ====================

@Composable
fun GameScreen(
    level: Int,
    adManager: AdManager,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current as Activity
    
    // للتحكم بكيبورد الهاتف الأساسي المدمج
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // للتحكم بنوافذ الإعلانات والعملات
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
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- 🌟 الجزء العلوي: القلوب (يمين)، اللوجو (وسط)، العملات (يسار) ---
            // نستخدم الترتيب العربي (اليمين أولاً) لسهولة التوجيه
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // القلوب (أعلى اليمين)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_heart_custom),
                        contentDescription = "Hearts Icon",
                        modifier = Modifier.size(60.dp)
                    )
                    Text(
                        text = state.lives.toString(), 
                        color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black,
                        style = TextStyle(shadow = Shadow(color = CrimsonRed, blurRadius = 10f))
                    )
                }

                // لوجو اللعبة صراع العقول بتصميمك الخاص (في المنتصف)
                Image(
                    painter = painterResource(id = R.drawable.logo_game),
                    contentDescription = "Mind Clash Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(80.dp).weight(1f).padding(horizontal = 10.dp)
                )

                // العملات (أعلى اليسار) - قابلة للضغط لزيادة العملات
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
                        contentDescription = "Coins Icon with +",
                        modifier = Modifier.size(60.dp)
                    )
                    Text(
                        text = state.score.toString(), 
                        color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black,
                        style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 10f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 🃏 حاوية السؤال (مستطيل بخلفيتك الخاصة) ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(220.dp) // يمكنك تعديل الارتفاع حسب صورتك
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_question),
                    contentDescription = "Question Container Background",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = state.currentQuestion?.question ?: "جاري تهيئة العقول...",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center,
                    lineHeight = 36.sp, modifier = Modifier.padding(25.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- ⌨️ نظام كيبورد الهاتف (المربعات المخفية والظاهرة) ---
            // حقل النص المخفي الذي يستدعي الكيبورد ويستقبل المدخلات
            BasicTextField(
                value = state.userAnswer,
                onValueChange = { viewModel.onNativeKeyboardInput(it) },
                modifier = Modifier.size(1.dp).alpha(0f).focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // المربعات الظاهرة للمستخدم (عند النقر على الصف كله يفتح الكيبورد)
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
                    
                    // تحكم بلمعان الإطار عند الكتابة
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
            
            // نص إرشادي صغير جداً
            Text(
                text = "انقر على المربعات للكتابة", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp,
                modifier = Modifier.padding(top = 10.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- 🆘 أزرار المساعدة السفلية (التلميح وكشف حرف بصورك الخاصة) ---
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                
                // زر التلميح (💡)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { if (state.score >= 50) viewModel.buyHint() else { adDialogType = "hint"; showAdDialog = true } }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_btn_hint),
                        contentDescription = "Tip/Hint Icon Button",
                        modifier = Modifier.size(70.dp).padding(5.dp)
                    )
                    Text("💡 تلميح", color = LiquidGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // زر كشف حرف (🔍)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { if (state.score >= 50) viewModel.buyRevealLetter() else { adDialogType = "letter"; showAdDialog = true } }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_btn_reveal),
                        contentDescription = "Reveal Letter Icon Button",
                        modifier = Modifier.size(70.dp).padding(5.dp)
                    )
                    Text("🔍 كشف حرف", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- 📺 نافذة الدايلوج الاحترافي (عند طلب زيادة العملات أو نفاذها) بصورك الخاصة ---
        if (showAdDialog) {
            val dialogTitle = if (adDialogType == "coins") "عملات مجانية!" else "رصيد غير كافٍ!"
            val dialogMessage = when (adDialogType) {
                "hint" -> "لقد نفذت العملات الذهبية، هل تريد مشاهدة إعلان لكشف تلميح؟"
                "letter" -> "لقد نفذت العملات الذهبية، هل تريد مشاهدة إعلان لكشف حرف؟"
                else -> "هل تريد مشاهدة إعلان للحصول على 50 عملة ذهبية؟"
            }
            
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(30.dp)).background(VoidBlack).border(2.dp, LiquidGold, RoundedCornerShape(30.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    
                    // أيقونة الإعلان النيونية من تصميمك
                    Image(
                        painter = painterResource(id = R.drawable.ic_watch_ad),
                        contentDescription = "Watch Ad Dialog Icon",
                        modifier = Modifier.size(100.dp)
                    )
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
                    Button(onClick = { showAdDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(55.dp)) {
                        Text("إلغاء", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- طبقات المؤثرات السينمائية (الصح، الخطأ، الفوز، الخسارة) بأكواد الصور ---
        AnimatedVisibility(visible = state.showCorrectAnimation, enter = fadeIn(), exit = fadeOut()) { NeoCorrectOverlayCustom() }
        AnimatedVisibility(visible = state.showWrongAnimation, enter = fadeIn(), exit = fadeOut()) { NeoWrongOverlayCustom() }
        
        AnimatedVisibility(visible = state.isLevelComplete, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) { 
            LegendaryResultOverlayCustom(title = "انتصار كاسح! 🎉", message = "تم تدمير أسرار المستوى ${state.currentLevel}!", score = state.score, isWin = true, buttonText = "اقتحم المستوى التالي ➡", buttonColor = NeonCyan, 
                onClick = { adManager.showInterstitialAd(context) { viewModel.loadLevel(state.currentLevel + 1) } }, 
                onGoHome = { adManager.showInterstitialAd(context) { onNavigateBack() } }) 
        }
        
        AnimatedVisibility(visible = state.isGameOver, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) { 
            LegendaryResultOverlayCustom(title = "سقطت العقول! 💀", message = "تم تدمير كل دفاعاتك في المستوى ${state.currentLevel}", score = state.score, isWin = false, buttonText = "انتقام (إعادة) 🔄", buttonColor = CrimsonRed, 
                onClick = { adManager.showInterstitialAd(context) { viewModel.resetGame() } }, 
                onGoHome = { adManager.showInterstitialAd(context) { onNavigateBack() } }) 
        }
    }
}

// ==================== المكونات المصغرة لتأثيرات النتائج (بالصور المخصصة) ====================

// 1. مؤثر الإجابة الصحيحة (صح النيونية)
@Composable
fun NeoCorrectOverlayCustom() {
    var scale by remember { mutableStateOf(0f) }
    var glowAlpha by remember { mutableStateOf(0.4f) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "")
    glowAlpha = infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 0.8f, animationSpec = infiniteRepeatable(animation = tween(400, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "").value
    
    LaunchedEffect(Unit) {
        scale = 1.2f
        delay(600)
        scale = 0f
    }
    
    Box(modifier = Modifier.fillMaxSize().background(EmeraldGreen.copy(alpha = 0.4f * glowAlpha)).alpha(scale), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.ic_status_correct),
            contentDescription = "Correct Answer Custom Neon Status",
            modifier = Modifier.fillMaxWidth(0.6f).aspectRatio(1f).scale(scale)
        )
    }
}

// 2. مؤثر الإجابة الخاطئة (خطأ النيونية)
@Composable
fun NeoWrongOverlayCustom() {
    var scale by remember { mutableStateOf(0f) }
    var shake by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1.2f
        shake = 15f
        delay(100)
        shake = -15f
        delay(100)
        shake = 10f
        delay(100)
        shake = 0f
        delay(400)
        scale = 0f
    }
    
    Box(modifier = Modifier.fillMaxSize().background(CrimsonRed.copy(alpha = 0.4f)).alpha(scale), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.ic_status_wrong),
            contentDescription = "Wrong Answer Custom Neon Status",
            modifier = Modifier.fillMaxWidth(0.6f).aspectRatio(1f).scale(scale).then(Modifier.offset(x = shake.dp))
        )
    }
}

// 3. شاشة النتيجة النهائية (الفوز/الخسارة بصورك الخاصة)
@Composable
fun LegendaryResultOverlayCustom(
    title: String,
    message: String,
    score: Int,
    isWin: Boolean,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit,
    onGoHome: () -> Unit
) {
    var scale by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
    }
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(0.9f).scale(scale).clip(RoundedCornerShape(40.dp)).background(VoidBlack).border(2.dp, if(isWin) LiquidGold else CrimsonRed, RoundedCornerShape(40.dp)).padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // أيقونة النتيجة الأسطورية النيونية من تصميمك (كأس أو جمجمة)
            Image(
                painter = painterResource(id = if(isWin) R.drawable.ic_state_win else R.drawable.ic_state_gameover),
                contentDescription = if(isWin) "Victory Cinematic Icon" else "Defeat Cinematic Icon",
                modifier = Modifier.fillMaxWidth(0.6f).aspectRatio(1f)
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Text(
                text = title, fontSize = 34.sp, fontWeight = FontWeight.Black, 
                color = if(isWin) LiquidGold else CrimsonRed, textAlign = TextAlign.Center,
                style = TextStyle(shadow = Shadow(color = if(isWin) LiquidGold else CrimsonRed, blurRadius = 15f))
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Text(text = message, fontSize = 20.sp, color = Color.White, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "نقاط المستوى: $score", fontSize = 26.sp, fontWeight = FontWeight.Black, color = LiquidGold,
                style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 10f))
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                
                // زر الإجراء الرئيسي (أزرق نيون أو أحمر قرمزي)
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, buttonColor),
                    modifier = Modifier.weight(1f).height(55.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                // زر الرئيسية الرمادي الشفاف
                Button(
                    onClick = onGoHome,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                    modifier = Modifier.weight(1f).height(55.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("🏠 الرئيسية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

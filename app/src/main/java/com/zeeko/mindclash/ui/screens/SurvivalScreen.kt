package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.content.Context
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
import com.zeeko.mindclash.AudioPlayer
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay

// قائمة أسئلة مؤقتة وعشوائية لطور النجاة (حتى نربط قاعدة البيانات)
val survivalQuestions = listOf(
    Pair("شيء كلما زاد نقص، ما هو؟", "العمر"),
    Pair("له أسنان كثيرة ولكنه لا يعض، ما هو؟", "المشط"),
    Pair("يسمع بلا أذن ويتكلم بلا لسان، ما هو؟", "الهاتف"),
    Pair("كلما أخذت منه كبر، ما هو؟", "الحفرة"),
    Pair("ما هو الشيء الذي يمشي بلا أرجل ويبكي بلا عيون؟", "السحاب"),
    Pair("ابن الماء وإذا وضع فيه مات، ما هو؟", "الثلج")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SurvivalScreen(
    adManager: AdManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current as Activity
    val prefs = context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE)
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // حالة اللعبة
    var timeLeft by remember { mutableIntStateOf(60) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(prefs.getInt("SurvivalHighScore", 0)) }
    var isGameOver by remember { mutableStateOf(false) }
    var isTimerRunning by remember { mutableStateOf(true) }
    
    // حالة السؤال والإجابة
    var currentQuestionIndex by remember { mutableIntStateOf(survivalQuestions.indices.random()) }
    val currentQuestion = survivalQuestions[currentQuestionIndex]
    var userAnswer by remember { mutableStateOf("") }
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    // تأثيرات
    var showCorrectAnim by remember { mutableStateOf(false) }
    var showWrongAnim by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition()
    val timerScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (timeLeft <= 10 && !isGameOver) 1.2f else 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "timer"
    )

    // مؤقت اللعبة
    LaunchedEffect(isTimerRunning, timeLeft) {
        if (isTimerRunning && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        } else if (timeLeft <= 0 && !isGameOver) {
            isGameOver = true
            isTimerRunning = false
            AudioPlayer.playLose()
            if (score > highScore) {
                highScore = score
                prefs.edit().putInt("SurvivalHighScore", highScore).apply()
            }
        }
    }

    // التحقق من الإجابة
    LaunchedEffect(userAnswer) {
        if (userAnswer.length == currentQuestion.second.length) {
            if (userAnswer == currentQuestion.second) {
                // إجابة صحيحة
                AudioPlayer.playCorrect()
                score += 10
                timeLeft += 5 // مكافأة وقت
                showCorrectAnim = true
                delay(500)
                showCorrectAnim = false
                userAnswer = ""
                textFieldValue = TextFieldValue("")
                currentQuestionIndex = survivalQuestions.indices.random()
            } else {
                // إجابة خاطئة
                AudioPlayer.playWrong()
                timeLeft = maxOf(0, timeLeft - 5) // عقاب وقت
                showWrongAnim = true
                delay(500)
                showWrongAnim = false
                userAnswer = ""
                textFieldValue = TextFieldValue("")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.bg_game), contentDescription = "Background", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize().padding(16.dp).imePadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(30.dp))

            // الشريط العلوي (الوقت والنقاط)
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // النقاط
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("النقاط", color = LiquidGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$score", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 10f)))
                }
                
                // الوقت
                Box(modifier = Modifier.size(90.dp).scale(timerScale).clip(CircleShape).background(if (timeLeft <= 10) CrimsonRed.copy(alpha = 0.5f) else VoidBlack.copy(alpha = 0.8f)).border(3.dp, if (timeLeft <= 10) CrimsonRed else NeonCyan, CircleShape), contentAlignment = Alignment.Center) {
                    Text(text = "$timeLeft", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                }

                // أعلى رقم
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("أعلى رقم", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$highScore", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // صندوق السؤال
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Image(painter = painterResource(id = R.drawable.bg_question), contentDescription = "Question", contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
                Text(text = currentQuestion.first, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, lineHeight = 36.sp, modifier = Modifier.padding(25.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // لوحة الإدخال
            val isArabicAnswer = currentQuestion.second.any { it in '\u0600'..'\u06FF' }
            
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    val cleanText = newValue.text.replace("\n", "")
                    if (cleanText.length <= currentQuestion.second.length) {
                        textFieldValue = newValue.copy(text = cleanText)
                        userAnswer = cleanText
                    }
                },
                modifier = Modifier.size(1.dp).alpha(0f).focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            CompositionLocalProvider(LocalLayoutDirection provides if (isArabicAnswer) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusRequester.requestFocus(); keyboardController?.show() },
                    horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 0 until currentQuestion.second.length) {
                        val char = userAnswer.getOrNull(i)?.toString() ?: ""
                        val borderColor = if (char.isNotEmpty()) NeonCyan else Color.White.copy(alpha = 0.5f)
                        Box(modifier = Modifier.padding(horizontal = 4.dp).size(60.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.15f)).border(2.dp, borderColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text(text = char, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, style = TextStyle(shadow = Shadow(color = borderColor, blurRadius = 8f)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(text = "إجابة صحيحة = +5 ثوانٍ | خاطئة = -5 ثوانٍ", color = CrimsonRed, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))
        }

        // المؤثرات (استخدام نفس الدوال الموجودة في GameScreen)
        AnimatedVisibility(visible = showCorrectAnim, enter = fadeIn(), exit = fadeOut()) { NeoCorrectOverlayCustom() }
        AnimatedVisibility(visible = showWrongAnim, enter = fadeIn(), exit = fadeOut()) { NeoWrongOverlayCustom() }

        // شاشة نهاية الوقت (فيها زر الإعلان)
        if (isGameOver) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.fillMaxWidth(0.9f).clip(RoundedCornerShape(40.dp)).background(VoidBlack).border(2.dp, CrimsonRed, RoundedCornerShape(40.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "انتهى الوقت! ⏳", fontSize = 34.sp, fontWeight = FontWeight.Black, color = CrimsonRed, style = TextStyle(shadow = Shadow(color = CrimsonRed, blurRadius = 15f)))
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = "النقاط التي جمعتها: $score", fontSize = 24.sp, color = LiquidGold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    // الزر الذهبي (المنقذ والمربح)
                    Button(onClick = {
                        AudioPlayer.playClick()
                        adManager.showRewardedAd(context, onRewardEarned = {
                            timeLeft = 15 // إضافة 15 ثانية
                            isGameOver = false
                            isTimerRunning = true
                        }, onAdFailed = { Toast.makeText(context, "الإعلان غير جاهز", Toast.LENGTH_SHORT).show() })
                    }, colors = ButtonDefaults.buttonColors(containerColor = LiquidGold.copy(alpha = 0.2f)), border = BorderStroke(2.dp, LiquidGold), modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(20.dp)) { 
                        Text("أنقذني بـ 15 ثانية (إعلان) 📺", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) 
                    }
                    
                    Spacer(modifier = Modifier.height(15.dp))
                    
                    Button(onClick = { AudioPlayer.playClick(); onNavigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), border = BorderStroke(1.dp, Color.Gray), modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(20.dp)) { 
                        Text("عودة للرئيسية 🏠", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) 
                    }
                }
            }
        }
    }
}

package com.zeeko.mindclash.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlin.random.Random

@Composable
fun IntroNeuralOverloadScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE) }
    
    // --- مراحل المشهد السينمائي ---
    // 0: الصمت المريب، 1: الإنذار والخطر، 2: الأكشن واللعب، 3: النجاح والانفجار الأبيض
    var currentPhase by remember { mutableIntStateOf(0) }
    
    // --- متغيرات اللعب (الأكشن) ---
    var stabilityLevel by remember { mutableFloatStateOf(0f) }
    val maxStability = 1f
    var activeNodes by remember { mutableStateOf(listOf<Offset>()) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    // --- مؤثرات الاهتزاز (Screen Shake) ---
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -15f, targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(50, easing = LinearEasing), RepeatMode.Reverse), label = "shake_anim"
    )

    // خطوة أمان احترافية: إيقاف الصوت لو خرج اللاعب فجأة من التطبيق
    DisposableEffect(Unit) {
        onDispose {
            AudioPlayer.stopHeartbeat()
        }
    }

    // --- إدارة المراحل زمنياً والأصوات ---
    LaunchedEffect(Unit) {
        // المرحلة 0: صمت مريب ودقات قلب بطيئة (3 ثوانٍ)
        AudioPlayer.startHeartbeat()
        delay(3000)
        
        // المرحلة 1: إنذار!
        currentPhase = 1
        AudioPlayer.stopHeartbeat() // نوقف النبض البطيء
        AudioPlayer.playAlarm()     // نطلق الإنذار المرعب!
        delay(2500)
        
        // المرحلة 2: بدء الأكشن (توليد عقد الطاقة العشوائية)
        currentPhase = 2
        // توليد أول 3 عقد ليضربها اللاعب
        activeNodes = List(3) {
            Offset(
                x = Random.nextInt(50, screenWidth - 50).toFloat(),
                y = Random.nextInt(150, screenHeight - 150).toFloat()
            )
        }
    }

    // --- واجهة المستخدم (UI) ---
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        // 1. صورة الخلفية (تظهر فقط بعد المرحلة 0 وتهتز في مرحلة الخطر)
        if (currentPhase > 0) {
            Image(
                painter = painterResource(id = R.drawable.bg_home), // استخدم صورة الخلفية الفخمة التي لديك
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // اهتزاز الشاشة إذا كنا في مرحلة الخطر (1 أو 2)
                        translationX = if (currentPhase in 1..2) shakeOffset else 0f
                        translationY = if (currentPhase in 1..2) shakeOffset else 0f
                    }
                    .alpha(if (currentPhase == 3) 1f else 0.5f)
            )
        }

        // 2. المحتوى حسب المرحلة
        when (currentPhase) {
            0 -> { // الصمت
                Text(
                    text = "جاري تهيئة العصب الذهني...",
                    color = Color.DarkGray,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            1 -> { // الإنذار
                val alphaPulse by infiniteTransition.animateFloat(
                    initialValue = 0.2f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(200), RepeatMode.Reverse), label = ""
                )
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚠️ تـحـذيـر ⚠️",
                        color = CrimsonRed,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.alpha(alphaPulse),
                        style = TextStyle(shadow = Shadow(color = CrimsonRed, blurRadius = 30f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "حمل زائد على النواة!\nالنظام ينهار!",
                        color = Color.White,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            2 -> { // الأكشن والضغط
                // شريط الاستقرار (النجاة)
                Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "اضغط على عقد الطاقة لتثبيت النواة!",
                        color = NeonCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(shadow = Shadow(color = NeonCyan, blurRadius = 15f))
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    // شريط التقدم
                    Box(modifier = Modifier.width(250.dp).height(20.dp).clip(CircleShape).background(Color.DarkGray).border(2.dp, NeonCyan, CircleShape)) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(stabilityLevel / maxStability).background(LiquidGold))
                    }
                }

                // رسم العقد التفاعلية (Nodes)
                activeNodes.forEachIndexed { index, offset ->
                    val nodePulse by infiniteTransition.animateFloat(
                        initialValue = 0.8f, targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse), label = ""
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = offset.x.dp, y = offset.y.dp)
                            .size(60.dp)
                            .scale(nodePulse)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.8f))
                            .border(3.dp, Color.White, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                AudioPlayer.playClick() // صوت ضرب العقدة
                                // زيادة الاستقرار
                                stabilityLevel += 0.15f
                                
                                // إذا اكتمل الاستقرار، ننتقل لمرحلة النجاح
                                if (stabilityLevel >= maxStability) {
                                    currentPhase = 3
                                } else {
                                    // إخفاء هذه العقدة وتوليد واحدة جديدة في مكان عشوائي
                                    val newNodes = activeNodes.toMutableList()
                                    newNodes[index] = Offset(
                                        x = Random.nextInt(50, screenWidth - 80).toFloat(),
                                        y = Random.nextInt(150, screenHeight - 150).toFloat()
                                    )
                                    activeNodes = newNodes
                                }
                            }
                    )
                }
            }
            3 -> { // النجاح والوميض الأبيض
                val whiteFlash = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    AudioPlayer.playPowerUp() // صوت تفريغ الطاقة والنجاح الفخم!
                    whiteFlash.animateTo(1f, tween(300)) // وميض سريع
                    delay(500)
                    whiteFlash.animateTo(0f, tween(1500)) // يتلاشى ببطء
                    delay(500)
                    
                    // حفظ أن اللاعب تجاوز الافتتاحية
                    sharedPreferences.edit().putBoolean("IntroOverloadComplete", true).apply()
                    onComplete()
                }

                // رسالة النجاح
                Text(
                    text = "تم استقرار النواة\nMINDCLASH -> CONNECTED",
                    color = NeonCyan,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(shadow = Shadow(color = NeonCyan, blurRadius = 20f))
                )

                // صندوق الوميض الأبيض
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = whiteFlash.value)))
            }
        }
    }
}

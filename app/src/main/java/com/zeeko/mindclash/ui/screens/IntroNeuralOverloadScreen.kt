package com.zeeko.mindclash.ui.screens

import android.content.Context
import androidx.compose.animation.Crossfade
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
import kotlinx.coroutines.launch
import kotlin.random.Random

// ✨ كلاس بسيط لإدارة حالة العقدة (مكانها وهل هي محطمة أم لا)
data class NodeState(
    val id: Int,
    val offset: Offset,
    var isBroken: Boolean = false
)

@Composable
fun IntroNeuralOverloadScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    
    // --- مراحل المشهد السينمائي ---
    var currentPhase by remember { mutableIntStateOf(0) }
    
    // --- متغيرات اللعب (الأكشن) - معدلة لتستمر طويلاً ---
    var stabilityLevel by remember { mutableFloatStateOf(0f) }
    val maxStability = 1f
    
    // قائمة العقد الآن تحمل "حالة العقدة" (NodeState) لتغيير شكلها
    var activeNodesStates by remember { mutableStateOf(listOf<NodeState>()) }
    
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    // --- مؤثرات الاهتزاز (Screen Shake) ---
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -18f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(45, easing = LinearEasing), RepeatMode.Reverse), label = "shake_anim"
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
        AudioPlayer.stopHeartbeat()
        AudioPlayer.playAlarm()
        delay(2500)
        
        // المرحلة 2: بدء الأكشن
        currentPhase = 2
        // توليد 6 عقد سليمة لزيادة التشتيت
        activeNodesStates = List(6) { index ->
            NodeState(
                id = index,
                offset = Offset(
                    x = Random.nextInt(50, screenWidth - 80).toFloat(),
                    y = Random.nextInt(180, screenHeight - 180).toFloat()
                )
            )
        }
    }

    // --- نظام "التفريغ التلقائي" السريع (Energy Decay) ---
    if (currentPhase == 2) {
        LaunchedEffect(Unit) {
            while (stabilityLevel < maxStability && currentPhase == 2) {
                delay(500) // كل نصف ثانية!
                if (stabilityLevel > 0f) {
                    // تفريغ مستمر وقاسٍ يجبره على الاستمرار في النقر
                    stabilityLevel -= 0.015f 
                    if (stabilityLevel < 0f) stabilityLevel = 0f
                }
            }
        }
    }

    // --- واجهة المستخدم (UI) ---
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        // 1. صورة الخلفية (تظهر فقط بعد المرحلة 0 وتهتز)
        if (currentPhase > 0) {
            Image(
                painter = painterResource(id = R.drawable.bg_home), 
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = if (currentPhase in 1..2) shakeOffset else 0f
                        translationY = if (currentPhase in 1..2) shakeOffset else 0f
                    }
                    .alpha(if (currentPhase == 3) 1f else 0.4f)
            )
        }

        // 2. المحتوى حسب المرحلة
        when (currentPhase) {
            0 -> {
                Text(
                    text = "جاري تهيئة العصب الذهني...",
                    color = Color.DarkGray,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            1 -> {
                val alphaPulse by infiniteTransition.animateFloat(
                    initialValue = 0.2f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(150), RepeatMode.Reverse), label = ""
                )
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚠️ تـحـذيـر ⚠️",
                        color = CrimsonRed,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.alpha(alphaPulse),
                        style = TextStyle(shadow = Shadow(color = CrimsonRed, blurRadius = 35f))
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        text = "حمل زائد على النواة!\nالنظام ينهار في غضون ثوانٍ!",
                        color = Color.White,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            2 -> {
                // شريط النجاة في الأعلى
                Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "اضغط بسرعة لتثبيت النواة قبل فوات الأوان!",
                        color = NeonCyan,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        style = TextStyle(shadow = Shadow(color = NeonCyan, blurRadius = 20f))
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier.width(280.dp).height(22.dp).clip(CircleShape).background(Color.DarkGray).border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(stabilityLevel / maxStability).background(LiquidGold))
                    }
                }

                // --- عقد الطاقة (تتغير من سليمة لمحطمة عند النقر) ---
                activeNodesStates.forEach { nodeState ->
                    val nodePulseTransition = rememberInfiniteTransition(label = "pulse")
                    val nodePulseScale by nodePulseTransition.animateFloat(
                        initialValue = 0.85f, targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(tween(Random.nextInt(150, 250), easing = FastOutSlowInEasing), RepeatMode.Reverse), label = ""
                    )
                    
                    // تم إزالة الألوان المسببة للخطأ والاكتفاء بالظل لضمان نجاح البناء
                    Box(
                        modifier = Modifier
                            .offset(x = nodeState.offset.x.dp, y = nodeState.offset.y.dp)
                            .size(80.dp)
                            .graphicsLayer {
                                shadowElevation = if (nodeState.isBroken) 30f else 15f
                            }
                    ) {
                        Crossfade(
                            targetState = nodeState.isBroken, 
                            animationSpec = tween(if (nodeState.isBroken) 50 else 0), 
                            label = "break"
                        ) { isBroken ->
                            Image(
                                painter = painterResource(
                                    id = if (isBroken) R.drawable.ic_neural_node_broken else R.drawable.ic_neural_node_healthy
                                ),
                                contentDescription = "Neural Node",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(nodePulseScale)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                            ) {
                                // التحقق أن العقدة ليست مكسورة مسبقاً لمنع النقر المتعدد
                                if (!nodeState.isBroken && currentPhase == 2) {
                                    
                                    AudioPlayer.playClick() 
                                    
                                    // تحديث حالة العقدة لتصبح مكسورة فوراً
                                    val updatedList = activeNodesStates.map {
                                        if (it.id == nodeState.id) it.copy(isBroken = true) else it
                                    }
                                    activeNodesStates = updatedList
                                    
                                    // زيادة شريط الثبات بنسبة بطيئة
                                    stabilityLevel += 0.015f
                                    
                                    if (stabilityLevel >= maxStability) {
                                        currentPhase = 3
                                    } else {
                                        // تأخير ليراها اللاعب محطمة قبل إخفائها ونقلها
                                        scope.launch {
                                            delay(200) 
                                            
                                            val newList = activeNodesStates.map {
                                                if (it.id == nodeState.id) {
                                                    it.copy(
                                                        isBroken = false,
                                                        offset = Offset(
                                                            x = Random.nextInt(40, screenWidth - 90).toFloat(),
                                                            y = Random.nextInt(180, screenHeight - 180).toFloat()
                                                        )
                                                    )
                                                } else it
                                            }
                                            activeNodesStates = newList
                                        }
                                    }
                                }
                            }
                            )
                        }
                    }
                }
            }
            3 -> { // النجاح والوميض الأبيض
                val whiteFlash = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    AudioPlayer.playPowerUp() 
                    whiteFlash.animateTo(1f, tween(300)) 
                    delay(500)
                    whiteFlash.animateTo(0f, tween(1500)) 
                    delay(1500) 
                    
                    sharedPreferences.edit().putBoolean("IntroOverloadComplete", true).apply()
                    onComplete()
                }

                // النص النهائي المطلوب
                Text(
                    text = "جيد يمكنك متابعة اللعب...",
                    color = NeonCyan,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(shadow = Shadow(color = NeonCyan, blurRadius = 25f))
                )

                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = whiteFlash.value)))
            }
        }
    }
}

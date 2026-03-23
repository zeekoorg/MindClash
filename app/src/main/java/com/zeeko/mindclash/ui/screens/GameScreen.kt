package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.game.GameViewModel
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ==================== تأثيرات خاصة متطورة ====================

@Composable
fun Modifier.glowEffect(
    color: Color = LiquidNeonCyan,
    radius: Float = 0.5f,
    alpha: Float = 0.5f
): Modifier = this.drawWithContent {
    drawContent()
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = size.minDimension * radius,
        blendMode = BlendMode.Screen
    )
}

@Composable
fun Modifier.rotate3D(
    pitch: Float = 0f,
    yaw: Float = 0f,
    roll: Float = 0f
): Modifier {
    return this.graphicsLayer {
        rotationX = pitch
        rotationY = yaw
        rotationZ = roll
        cameraDistance = 12f * density
    }
}

@Composable
fun Modifier.particleEffect(
    particleCount: Int = 50,
    color: Color = LiquidNeonCyan
): Modifier = this.drawWithContent {
    drawContent()
    repeat(particleCount) { index ->
        val angle = (index * 360f / particleCount) + (System.currentTimeMillis() % 3600) / 10f
        val radius = size.minDimension * 0.3f
        val x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
        val y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * radius
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = 2f,
            center = Offset(x, y)
        )
    }
}

// ==================== الشاشة الرئيسية ====================

@OptIn(ExperimentalFoundationApi::class)
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
    val configuration = LocalConfiguration.current
    
    var showAdDialog by remember { mutableStateOf(false) }
    var adDialogType by remember { mutableStateOf("") }
    var rotationAngle by remember { mutableStateOf(0f) }
    
    // تأثير دوران الخلفية
    LaunchedEffect(Unit) {
        while (true) {
            rotationAngle = (rotationAngle + 0.3f) % 360f
            delay(50)
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadLevel(level)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cosmicGradient())
    ) {
        // طبقة الموجات النيونية
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..5) {
                val angle = rotationAngle + (i * 60f)
                val radius = 200f + i * 80f
                drawArc(
                    color = LiquidNeonCyan.copy(alpha = 0.1f),
                    startAngle = angle,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
                drawArc(
                    color = LiquidNeonMagenta.copy(alpha = 0.1f),
                    startAngle = angle + 180,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
            }
        }
        
        // المحتوى الرئيسي
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // الهيدر المتطور
            CosmicHeader(
                lives = state.lives,
                score = state.score,
                level = level
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // التلميح ثلاثي الأبعاد
            AnimatedVisibility(
                visible = state.isHintVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn() + scaleIn(initialScale = 0.5f),
                exit = slideOutVertically() + fadeOut() + scaleOut()
            ) {
                HolographicHint(hint = state.currentQuestion?.hint ?: "")
            }
            
            Spacer(modifier = Modifier.height(25.dp))
            
            // بطاقة السؤال الكونية
            CosmicQuestionCard(
                question = state.currentQuestion?.question ?: "جاري تهيئة العقول...",
                showCorrect = state.showCorrectAnimation,
                showWrong = state.showWrongAnimation
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // مربعات الإجابة السحرية
            MagicAnswerBoxes(
                answerLength = state.currentQuestion?.answer?.length ?: 0,
                userAnswer = state.userAnswer,
                showCorrect = state.showCorrectAnimation,
                showWrong = state.showWrongAnimation
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // أزرار المساعدة الكونية
            CosmicHelpButtons(
                isHintVisible = state.isHintVisible,
                score = state.score,
                onBuyHint = {
                    if (state.score >= 50) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.buyHint()
                    } else {
                        adDialogType = "hint"
                        showAdDialog = true
                    }
                },
                onBuyLetter = {
                    if (state.score >= 50) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.buyRevealLetter()
                    } else {
                        adDialogType = "letter"
                        showAdDialog = true
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // لوحة المفاتيح السحرية
            MagicKeyboard(
                onLetterClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onLetterClick(it)
                },
                onDeleteClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onDeleteClick()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // نافذة الإعلان
        if (showAdDialog) {
            CosmicAdDialog(
                type = adDialogType,
                onDismiss = { showAdDialog = false },
                onWatchAd = {
                    showAdDialog = false
                    adManager.showRewardedAd(
                        activity = context,
                        onRewardEarned = {
                            if (adDialogType == "hint") viewModel.showPermanentHint()
                            else viewModel.revealLetterFree()
                        },
                        onAdFailed = {
                            Toast.makeText(context, "الإعلان غير جاهز حالياً", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
        
        // شاشات النتائج السينمائية
        if (state.showCorrectAnimation) {
            CosmicSuccessAnimation()
        }
        if (state.showWrongAnimation) {
            CosmicFailureAnimation()
        }
        if (state.isLevelComplete) {
            CosmicVictoryCinematic(
                level = state.currentLevel,
                score = state.score,
                onNext = {
                    adManager.showInterstitialAd(context) {
                        viewModel.loadLevel(state.currentLevel + 1)
                    }
                },
                onHome = {
                    adManager.showInterstitialAd(context) {
                        onNavigateBack()
                    }
                }
            )
        }
        if (state.isGameOver) {
            CosmicDefeatCinematic(
                level = state.currentLevel,
                score = state.score,
                onRestart = {
                    adManager.showInterstitialAd(context) {
                        viewModel.resetGame()
                    }
                },
                onHome = {
                    adManager.showInterstitialAd(context) {
                        onNavigateBack()
                    }
                }
            )
        }
    }
}

// ==================== مكونات الهيدر الكوني ====================

@Composable
fun CosmicHeader(lives: Int, score: Int, level: Int) {
    var scale by remember { mutableStateOf(1f) }
    var rotationY by remember { mutableStateOf(0f) }
    
    LaunchedEffect(lives) {
        scale = 1.1f
        rotationY = 10f
        delay(200)
        scale = 1f
        rotationY = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .scale(scale)
            .rotate3D(yaw = rotationY)
            .clip(RoundedCornerShape(32.dp))
            .background(GlassCrystal)
            .border(
                width = 2.dp,
                brush = neonWaveGradient(),
                shape = RoundedCornerShape(32.dp)
            )
            .glowEffect(color = LiquidNeonGold, radius = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // القلوب المتوهجة
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isActive = index < lives
                    val pulseScale by animateFloatAsState(
                        targetValue = if (isActive) 1f else 0.8f,
                        animationSpec = repeatable(
                            iterations = Int.MAX_VALUE,
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .scale(pulseScale)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_heart_glass),
                            contentDescription = null,
                            tint = if (isActive) HeartGlow else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            
            // المستوى المتوهج
            Text(
                text = "الْمَرْحَلَةُ $level",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(LiquidNeonGold, LiquidNeonCyan)
                    )
                ),
                modifier = Modifier.glowEffect(color = LiquidNeonGold, radius = 0.4f)
            )
            
            // العملات المتألقة
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$score",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = RoyalGold,
                    style = TextStyle(
                        shadow = Shadow(
                            color = RoyalGold.copy(alpha = 0.5f),
                            blurRadius = 8f
                        )
                    )
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_coin_glass),
                    contentDescription = null,
                    tint = RoyalGold,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

// ==================== التلميح الهولوغرافي ====================

@Composable
fun HolographicHint(hint: String) {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    var glowAlpha by remember { mutableStateOf(0.5f) }
    
    val infiniteTransition = rememberInfiniteTransition()
    glowAlpha = infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    ).value
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .rotate3D(pitch = rotationX, yaw = rotationY)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LiquidNeonCyan.copy(alpha = 0.2f),
                        LiquidNeonMagenta.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(LiquidNeonCyan, LiquidNeonMagenta, LiquidNeonCyan)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    rotationX = (rotationX + dragAmount.y * 0.15f).coerceIn(-12f, 12f)
                    rotationY = (rotationY + dragAmount.x * 0.15f).coerceIn(-12f, 12f)
                    change.consume()
                }
            }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨",
                fontSize = 28.sp,
                modifier = Modifier.glowEffect(color = LiquidNeonCyan)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = hint,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextGlow,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "✨",
                fontSize = 28.sp,
                modifier = Modifier.glowEffect(color = LiquidNeonMagenta)
            )
        }
    }
}

// ==================== بطاقة السؤال الكونية ====================

@Composable
fun CosmicQuestionCard(
    question: String,
    showCorrect: Boolean,
    showWrong: Boolean
) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var cardGlow by remember { mutableStateOf(LiquidNeonCyan) }
    
    LaunchedEffect(showCorrect, showWrong) {
        if (showCorrect) {
            scale = 1.08f
            cardGlow = EmeraldGreen
            delay(250)
            scale = 1f
            cardGlow = LiquidNeonCyan
        } else if (showWrong) {
            rotation = 8f
            cardGlow = CrimsonRed
            delay(100)
            rotation = -8f
            delay(100)
            rotation = 0f
            cardGlow = LiquidNeonCyan
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .scale(scale)
            .rotate(rotation)
            .clip(RoundedCornerShape(40.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xCC000000),
                        Color(0xCC1A0F2E),
                        Color(0xCC000000)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(cardGlow, LiquidNeonMagenta, cardGlow)
                ),
                shape = RoundedCornerShape(40.dp)
            )
            .glowEffect(color = cardGlow, radius = 0.4f)
            .animateContentSize()
            .padding(40.dp)
    ) {
        Text(
            text = question,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextGlow,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = cardGlow.copy(alpha = 0.5f),
                    blurRadius = 12f,
                    offset = Offset(2f, 2f)
                )
            )
        )
    }
}

// ==================== مربعات الإجابة السحرية ====================

@Composable
fun MagicAnswerBoxes(
    answerLength: Int,
    userAnswer: String,
    showCorrect: Boolean,
    showWrong: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val borderGlow by infiniteTransition.animateColor(
        initialValue = LiquidNeonCyan,
        targetValue = LiquidNeonMagenta,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val finalBorderColor = when {
        showCorrect -> EmeraldGreen
        showWrong -> CrimsonRed
        else -> borderGlow
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0 until answerLength) {
            val char = userAnswer.getOrNull(i)?.toString() ?: ""
            var boxScale by remember { mutableStateOf(1f) }
            var boxRotation by remember { mutableStateOf(0f) }
            
            LaunchedEffect(char) {
                if (char.isNotEmpty()) {
                    boxScale = 1.2f
                    boxRotation = 12f
                    delay(120)
                    boxScale = 1f
                    boxRotation = 0f
                }
            }
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(70.dp)
                    .scale(boxScale)
                    .rotate(boxRotation)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GlassCyan.copy(alpha = 0.8f),
                                GlassPurple.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        color = finalBorderColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .glowEffect(color = finalBorderColor, radius = 0.3f)
                    .animateContentSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = TextGlow,
                    style = TextStyle(
                        shadow = Shadow(
                            color = finalBorderColor,
                            blurRadius = 12f
                        )
                    )
                )
            }
        }
    }
}

// ==================== أزرار المساعدة الكونية ====================

@Composable
fun CosmicHelpButtons(
    isHintVisible: Boolean,
    score: Int,
    onBuyHint: () -> Unit,
    onBuyLetter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        CosmicActionButton(
            icon = "💡",
            text = "تَلْمِيح",
            price = 50,
            color = LiquidNeonGold,
            enabled = !isHintVisible,
            onClick = onBuyHint,
            modifier = Modifier.weight(1f)
        )
        
        CosmicActionButton(
            icon = "🔮",
            text = "حَرْف",
            price = 50,
            color = LiquidNeonCyan,
            enabled = true,
            onClick = onBuyLetter,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CosmicActionButton(
    icon: String,
    text: String,
    price: Int,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var buttonScale by remember { mutableStateOf(1f) }
    
    Box(
        modifier = modifier
            .height(65.dp)
            .scale(buttonScale)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.25f),
                        color.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = color,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = enabled) {
                buttonScale = 0.92f
                onClick()
                buttonScale = 1f
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(LiquidNeonGold.copy(alpha = 0.25f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$price 🪙",
                    color = LiquidNeonGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// ==================== لوحة المفاتيح السحرية ====================

@Composable
fun MagicKeyboard(
    onLetterClick: (Char) -> Unit,
    onDeleteClick: () -> Unit
) {
    var isNumeric by remember { mutableStateOf(false) }
    var keyboardGlow by remember { mutableStateOf(0f) }
    
    val infiniteTransition = rememberInfiniteTransition()
    keyboardGlow = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    ).value
    
    val letterRows = listOf(
        listOf('ج', 'ح', 'خ', 'ه', 'ع', 'غ', 'ف', 'ق', 'ص'),
        listOf('ض', 'ك', 'م', 'ن', 'ت', 'ا', 'ل', 'ب', 'ي'),
        listOf('س', 'ش', 'ء', 'ث', 'ة', 'و', 'ر', 'ز', 'د'),
        listOf('ذ', 'ط', 'ظ', 'ى', 'ئ', 'ؤ', 'إ', 'أ', 'آ')
    )
    
    val numberRows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9')
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val activeRows = if (isNumeric) numberRows else letterRows
        
        activeRows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { char ->
                    var keyScale by remember { mutableStateOf(1f) }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .scale(keyScale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        GlassCrystal,
                                        GlassCyan.copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        LiquidNeonCyan.copy(alpha = keyboardGlow),
                                        LiquidNeonMagenta.copy(alpha = keyboardGlow)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                keyScale = 0.85f
                                onLetterClick(char)
                                keyScale = 1f
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGlow,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = LiquidNeonCyan,
                                    blurRadius = 6f
                                )
                            )
                        )
                    }
                }
            }
        }
        
        // الصف السفلي
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MagicKeyboardButton(
                text = if (isNumeric) "أبجد" else "١٢٣",
                onClick = { isNumeric = !isNumeric },
                color = LiquidNeonMagenta,
                modifier = Modifier.weight(1f)
            )
            
            MagicKeyboardButton(
                text = "␣ مَسَافَة",
                onClick = { onLetterClick(' ') },
                color = LiquidNeonCyan,
                modifier = Modifier.weight(2f)
            )
            
            if (isNumeric) {
                MagicKeyboardButton(
                    text = "٠",
                    onClick = { onLetterClick('0') },
                    color = LiquidNeonCyan,
                    modifier = Modifier.weight(1f)
                )
                MagicKeyboardButton(
                    text = "⌫",
                    onClick = onDeleteClick,
                    color = LiquidNeonRed,
                    modifier = Modifier.weight(1f)
                )
            } else {
                MagicKeyboardButton(
                    text = "⌫ مَسْح",
                    onClick = onDeleteClick,
                    color = LiquidNeonRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MagicKeyboardButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    var buttonScale by remember { mutableStateOf(1f) }
    
    Box(
        modifier = modifier
            .height(60.dp)
            .scale(buttonScale)
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.5.dp, color, RoundedCornerShape(18.dp))
            .clickable {
                buttonScale = 0.92f
                onClick()
                buttonScale = 1f
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(
                    color = color.copy(alpha = 0.5f),
                    blurRadius = 8f
                )
            )
        )
    }
}

// ==================== نافذة الإعلان الكونية ====================

@Composable
fun CosmicAdDialog(
    type: String,
    onDismiss: () -> Unit,
    onWatchAd: () -> Unit
) {
    var dialogScale by remember { mutableStateOf(0f) }
    var dialogRotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        dialogScale = 1f
        dialogRotation = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .scale(dialogScale)
                .rotate(dialogRotation)
                .clip(RoundedCornerShape(40.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF0F0F1A)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = neonWaveGradient(),
                    shape = RoundedCornerShape(40.dp)
                )
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🪙✨🪙",
                fontSize = 72.sp,
                modifier = Modifier.glowEffect(color = LiquidNeonGold, radius = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "نَفَذَتِ الْعُمْلاتُ!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = LiquidNeonRed,
                style = TextStyle(
                    shadow = Shadow(
                        color = LiquidNeonRed,
                        blurRadius = 12f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = if (type == "hint") 
                    "✨ شَاهِدْ إِعْلاناً لِتُحَصِّلَ تَلْمِيحاً مَجَّانِيّاً ✨"
                else 
                    "🔮 شَاهِدْ إِعْلاناً لِتُحَصِّلَ حَرْفاً مَجَّانِيّاً 🔮",
                fontSize = 18.sp,
                color = TextMystic,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            MagicKeyboardButton(
                text = "📺 شَاهِدُ الْإِعْلَانَ",
                onClick = onWatchAd,
                color = LiquidNeonCyan,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MagicKeyboardButton(
                text = "❌ إِلْغَاءُ",
                onClick = onDismiss,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==================== أنيميشنات النجاح والفشل ====================

@Composable
fun CosmicSuccessAnimation() {
    var scale by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1.2f
        rotation = 360f
        delay(800)
        scale = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        EmeraldGreen.copy(alpha = 0.8f),
                        Color.Transparent
                    ),
                    radius = 500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✓✓✓ صَحِيحٌ! ✓✓✓",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = TextGlow,
                modifier = Modifier
                    .scale(scale)
                    .rotate(rotation)
                    .glowEffect(color = EmeraldGreen, radius = 0.6f)
            )
            Text(
                text = "✨ إِجَابَةٌ مُذْهِلَةٌ ✨",
                fontSize = 24.sp,
                color = TextGlow,
                modifier = Modifier.scale(scale)
            )
        }
    }
}

@Composable
fun CosmicFailureAnimation() {
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
        delay(600)
        scale = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        CrimsonRed.copy(alpha = 0.7f),
                        Color.Transparent
                    ),
                    radius = 500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✗✗✗ خَطَأٌ! ✗✗✗",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = TextGlow,
                modifier = Modifier
                    .scale(scale)
                    .rotate(shake)
                    .glowEffect(color = CrimsonRed, radius = 0.6f)
            )
            Text(
                text = "💀 حَاوِلْ مَرَّةً أُخْرَى 💀",
                fontSize = 24.sp,
                color = TextGlow,
                modifier = Modifier.scale(scale)
            )
        }
    }
}

// ==================== شاشات النتائج السينمائية ====================

@Composable
fun CosmicVictoryCinematic(
    level: Int,
    score: Int,
    onNext: () -> Unit,
    onHome: () -> Unit
) {
    var cinematicScale by remember { mutableStateOf(0f) }
    var starsRotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        cinematicScale = 1f
        starsRotation = 360f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        LiquidNeonGold.copy(alpha = 0.8f),
                        CosmicBlack
                    ),
                    radius = 800f
                )
            )
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .scale(cinematicScale)
                .clip(RoundedCornerShape(48.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xDD000000),
                            Color(0xCC1A0F2E)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(LiquidNeonGold, LiquidNeonCyan, LiquidNeonGold)
                    ),
                    shape = RoundedCornerShape(48.dp)
                )
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 نَصْرٌ سَاحِقٌ! 🏆",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = LiquidNeonGold,
                style = TextStyle(
                    shadow = Shadow(
                        color = LiquidNeonGold.copy(alpha = 0.6f),
                        blurRadius = 20f
                    )
                ),
                modifier = Modifier.glowEffect(color = LiquidNeonGold)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "أَكْمَلْتَ الْمَرْحَلَةَ $level",
                fontSize = 26.sp,
                color = TextGlow
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "نُقَاطُكَ: $score",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidNeonGold,
                style = TextStyle(
                    shadow = Shadow(
                        color = LiquidNeonGold.copy(alpha = 0.4f),
                        blurRadius = 12f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                MagicKeyboardButton(
                    text = "🚀 الْمَرْحَلَةُ التَّالِيَةُ",
                    onClick = onNext,
                    color = LiquidNeonCyan,
                    modifier = Modifier.weight(1f)
                )
                
                MagicKeyboardButton(
                    text = "🏠 الرَّئِيسِيَّةُ",
                    onClick = onHome,
                    color = LiquidNeonMagenta,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CosmicDefeatCinematic(
    level: Int,
    score: Int,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    var cinematicScale by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        cinematicScale = 1f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        CrimsonRed.copy(alpha = 0.7f),
                        CosmicBlack
                    ),
                    radius = 800f
                )
            )
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .scale(cinematicScale)
                .clip(RoundedCornerShape(48.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xDD000000),
                            Color(0xCC4A0000)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    color = CrimsonRed,
                    shape = RoundedCornerShape(48.dp)
                )
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💀 هَزِيمَةٌ نُكْرَاءُ 💀",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = CrimsonRed,
                style = TextStyle(
                    shadow = Shadow(
                        color = CrimsonRed.copy(alpha = 0.6f),
                        blurRadius = 20f
                    )
                ),
                modifier = Modifier.glowEffect(color = CrimsonRed)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "سَقَطْتَ فِي الْمَرْحَلَةِ $level",
                fontSize = 26.sp,
                color = TextGlow
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "مَجْمُوعُ نُقَاطِكَ: $score",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidNeonGold,
                style = TextStyle(
                    shadow = Shadow(
                        color = LiquidNeonGold.copy(alpha = 0.4f),
                        blurRadius = 12f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                MagicKeyboardButton(
                    text = "🔄 إِعَادَةُ الْمُحَاوَلَةِ",
                    onClick = onRestart,
                    color = CrimsonRed,
                    modifier = Modifier.weight(1f)
                )
                
                MagicKeyboardButton(
                    text = "🏠 الرَّئِيسِيَّةُ",
                    onClick = onHome,
                    color = LiquidNeonMagenta,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

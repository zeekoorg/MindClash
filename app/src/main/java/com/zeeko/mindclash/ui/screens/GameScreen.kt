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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.game.GameViewModel
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// 🌟 تأثيرات إضافية متطورة
@Composable
fun Modifier.animatedGlow(
    glowColor: Color = Color.Cyan,
    enabled: Boolean = true
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    if (enabled) {
        this.drawWithContent {
            drawContent()
            drawCircle(
                color = glowColor.copy(alpha = glowAlpha),
                radius = size.minDimension * 0.5f,
                blendMode = BlendMode.Screen
            )
        }
    } else this
}

@Composable
fun Modifier.rotate3D(
    pitch: Float = 0f,
    yaw: Float = 0f
): Modifier {
    val rotationX = pitch
    val rotationY = yaw
    
    return graphicsLayer {
        this.rotationX = rotationX
        this.rotationY = rotationY
        this.cameraDistance = 8 * density
    }
}

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
    var backgroundRotation by remember { mutableStateOf(0f) }
    var particleOffset by remember { mutableStateOf(Offset.Zero) }
    
    // تأثير دوران الخلفية المستمر
    LaunchedEffect(Unit) {
        while (true) {
            backgroundRotation = (backgroundRotation + 0.5f) % 360f
            particleOffset = Offset(
                x = (particleOffset.x + 2f) % configuration.screenWidthDp,
                y = (particleOffset.y + 1f) % configuration.screenHeightDp
            )
            delay(50)
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadLevel(level)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0A0F2A),
                        Color(0xFF050714),
                        Color(0xFF020208)
                    ),
                    radius = 1500f
                )
            )
    ) {
        // 🌌 طبقة الجزيئات ثلاثية الأبعاد
        Particle3DLayer(offset = particleOffset)
        
        // ✨ طبقة النيون المتوهجة
        NeonWaveLayer(rotation = backgroundRotation)
        
        // المحتوى الرئيسي
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // 👑 الرأسية الفاخرة
            AnimatedHeaderSection(
                lives = state.lives,
                score = state.score,
                level = level
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 💡 التلميح ثلاثي الأبعاد
            AnimatedVisibility(
                visible = state.isHintVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = 0.6f)
                ) + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                GlassHint3D(hint = state.currentQuestion?.hint ?: "")
            }
            
            Spacer(modifier = Modifier.height(25.dp))
            
            // 🃏 بطاقة السؤال بتأثير ثلاثي الأبعاد
            QuestionCard3D(
                question = state.currentQuestion?.question ?: "جاري التحميل...",
                showCorrect = state.showCorrectAnimation,
                showWrong = state.showWrongAnimation
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 🔠 مربعات الإجابة المتطورة
            AnswerBoxes3D(
                answerLength = state.currentQuestion?.answer?.length ?: 0,
                userAnswer = state.userAnswer,
                showCorrect = state.showCorrectAnimation,
                showWrong = state.showWrongAnimation
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 🆘 أزرار المساعدة بتأثير نيون
            HelpButtons3D(
                isHintVisible = state.isHintVisible,
                score = state.score,
                onBuyHint = {
                    if (state.score >= 50) viewModel.buyHint()
                    else { adDialogType = "hint"; showAdDialog = true }
                },
                onBuyLetter = {
                    if (state.score >= 50) viewModel.buyRevealLetter()
                    else { adDialogType = "letter"; showAdDialog = true }
                }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // ⌨️ لوحة المفاتيح ثلاثية الأبعاد
            NeonKeyboard3D(
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
        
        // 📺 نافذة الإعلان المتطورة
        if (showAdDialog) {
            AdDialog3D(
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
                            Toast.makeText(context, "الإعلان غير جاهز", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
        
        // 🎉 شاشات النتائج السينمائية
        if (state.showCorrectAnimation) {
            CorrectAnimation3D()
        }
        if (state.showWrongAnimation) {
            WrongAnimation3D()
        }
        if (state.isLevelComplete) {
            LevelCompleteCinematic(
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
            GameOverCinematic(
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

// 🌟 المكونات المتطورة

@Composable
fun Particle3DLayer(offset: Offset) {
    val particles = remember {
        List(100) { index ->
            Particle(
                x = Random.nextInt(0, 1000) / 10f,
                y = Random.nextInt(0, 2000) / 10f,
                size = Random.nextInt(2, 6),
                speed = Random.nextFloat() * 2 + 1,
                color = Color.White.copy(alpha = Random.nextFloat() * 0.5f)
            )
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val x = (particle.x + offset.x * particle.speed) % size.width
            val y = (particle.y + offset.y * particle.speed) % size.height
            
            drawCircle(
                color = particle.color,
                radius = particle.size.toFloat(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun NeonWaveLayer(rotation: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        for (i in 0..5) {
            val angle = rotation + (i * 60f)
            val radius = 200f + i * 50f
            
            drawArc(
                color = Color.Cyan.copy(alpha = 0.1f),
                startAngle = angle,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }
    }
}

@Composable
fun AnimatedHeaderSection(lives: Int, score: Int, level: Int) {
    var rotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            rotation = (rotation + 2f) % 360f
            delay(50)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        // خلفية زجاجية متوهجة
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x33FFFFFF),
                            Color(0x11FFFFFF),
                            Color(0x33FFFFFF)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Cyan, Color.Magenta)
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
        )
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // القلوب المتطورة
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val isActive = index < lives
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                            .rotate3D(pitch = rotation, yaw = rotation)
                    ) {
                        Icon(
                            imageVector = if (isActive) ImageVector.vectorResource(R.drawable.ic_heart_glass)
                                else ImageVector.vectorResource(R.drawable.ic_heart_glass),
                            contentDescription = null,
                            tint = if (isActive) Color.Red else Color.Gray,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            
            // المستوى بنيون متوهج
            Text(
                text = "المستوى $level",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Cyan, Color.Magenta)
                    )
                ),
                modifier = Modifier.animatedGlow(glowColor = Color.Cyan)
            )
            
            // العملات المتطورة
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$score",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "🪙",
                    fontSize = 32.sp
                )
            }
        }
    }
}

@Composable
fun GlassHint3D(hint: String) {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .rotate3D(pitch = rotationX, yaw = rotationY)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xCC00CED1),
                        Color(0x6600CED1)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Cyan, Color.White, Color.Cyan)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    rotationX = (rotationX + dragAmount.y * 0.2f).coerceIn(-15f, 15f)
                    rotationY = (rotationY + dragAmount.x * 0.2f).coerceIn(-15f, 15f)
                    change.consume()
                }
            }
            .padding(16.dp)
    ) {
        Text(
            text = "💡 $hint",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun QuestionCard3D(
    question: String,
    showCorrect: Boolean,
    showWrong: Boolean
) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(showCorrect, showWrong) {
        if (showCorrect) {
            scale = 1.1f
            delay(200)
            scale = 1f
        } else if (showWrong) {
            rotation = 10f
            delay(100)
            rotation = -10f
            delay(100)
            rotation = 0f
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .scale(scale)
            .rotate(rotation)
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xCC000000),
                        Color(0xCC1A1A2E),
                        Color(0xCC000000)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(Color.Cyan, Color.Magenta, Color.Cyan)
                ),
                shape = RoundedCornerShape(30.dp)
            )
            .animateContentSize()
            .padding(40.dp)
    ) {
        Text(
            text = question,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Cyan,
                    blurRadius = 10f,
                    offset = Offset(2f, 2f)
                )
            )
        )
    }
}

@Composable
fun AnswerBoxes3D(
    answerLength: Int,
    userAnswer: String,
    showCorrect: Boolean,
    showWrong: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val borderColor by infiniteTransition.animateColor(
        initialValue = Color.Cyan,
        targetValue = Color.Magenta,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0 until answerLength) {
            val char = userAnswer.getOrNull(i)?.toString() ?: ""
            var scale by remember { mutableStateOf(1f) }
            var rotation by remember { mutableStateOf(0f) }
            
            LaunchedEffect(char) {
                if (char.isNotEmpty()) {
                    scale = 1.2f
                    rotation = 10f
                    delay(150)
                    scale = 1f
                    rotation = 0f
                }
            }
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(65.dp)
                    .scale(scale)
                    .rotate(rotation)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xAA00CED1),
                                Color(0xAA1A1A2E)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = if (showCorrect) Color.Green else if (showWrong) Color.Red else borderColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .animateContentSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = borderColor,
                            blurRadius = 8f
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun HelpButtons3D(
    isHintVisible: Boolean,
    score: Int,
    onBuyHint: () -> Unit,
    onBuyLetter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NeonHelpButton(
            text = "💡 تلميح",
            price = 50,
            color = Color(0xFFFFD700),
            enabled = !isHintVisible,
            onClick = onBuyHint,
            modifier = Modifier.weight(1f)
        )
        
        NeonHelpButton(
            text = "🔍 حرف",
            price = 50,
            color = Color.Cyan,
            enabled = true,
            onClick = onBuyLetter,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NeonHelpButton(
    text: String,
    price: Int,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier
            .height(60.dp)
            .scale(scale)
            .rotate(rotation)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.2f),
                        color.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = color,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(enabled = enabled) {
                scale = 0.95f
                rotation = 2f
                onClick()
                scale = 1f
                rotation = 0f
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$price 🪙",
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun NeonKeyboard3D(
    onLetterClick: (Char) -> Unit,
    onDeleteClick: () -> Unit
) {
    var isNumeric by remember { mutableStateOf(false) }
    var keyboardRotation by remember { mutableStateOf(0f) }
    
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
            .rotate3D(pitch = 2f, yaw = keyboardRotation)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val activeRows = if (isNumeric) numberRows else letterRows
        
        activeRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { char ->
                    var scale by remember { mutableStateOf(1f) }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .scale(scale)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x33FFFFFF),
                                        Color(0x11FFFFFF)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Cyan, Color.Magenta)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                scale = 0.9f
                                onLetterClick(char)
                                scale = 1f
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Cyan,
                                    blurRadius = 4f
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button3D(
                text = if (isNumeric) "أبج" else "123",
                onClick = { isNumeric = !isNumeric },
                color = Color.Magenta,
                modifier = Modifier.weight(1f)
            )
            
            Button3D(
                text = "مسافة",
                onClick = { onLetterClick(' ') },
                color = Color.Cyan,
                modifier = Modifier.weight(2f)
            )
            
            if (isNumeric) {
                Button3D(
                    text = "0",
                    onClick = { onLetterClick('0') },
                    color = Color.Cyan,
                    modifier = Modifier.weight(1f)
                )
                Button3D(
                    text = "⌫",
                    onClick = onDeleteClick,
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Button3D(
                    text = "⌫ مسح",
                    onClick = onDeleteClick,
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun Button3D(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    
    Box(
        modifier = modifier
            .height(55.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .border(
                width = 1.dp,
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                scale = 0.95f
                onClick()
                scale = 1f
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AdDialog3D(
    type: String,
    onDismiss: () -> Unit,
    onWatchAd: () -> Unit
) {
    var scale by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
        rotation = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .scale(scale)
                .rotate(rotation)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(Color.Cyan, Color.Magenta, Color.Cyan)
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🪙",
                fontSize = 80.sp,
                modifier = Modifier.animatedGlow(glowColor = Color.Yellow)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "نفذت العملات!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.Red,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Red,
                        blurRadius = 10f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (type == "hint") "شاهد إعلاناً لتحصل على تلميح مجاني" 
                       else "شاهد إعلاناً لتحصل على حرف مجاني",
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Button3D(
                text = "📺 مشاهدة إعلان",
                onClick = onWatchAd,
                color = Color.Cyan,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Button3D(
                text = "❌ إلغاء",
                onClick = onDismiss,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CorrectAnimation3D() {
    var scale by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
        rotation = 360f
        delay(800)
        scale = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Green.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "✓✓✓ صحيح! ✓✓✓",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier
                .scale(scale)
                .rotate(rotation)
                .animatedGlow(glowColor = Color.Green)
        )
    }
}

@Composable
fun WrongAnimation3D() {
    var scale by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
        rotation = -360f
        delay(800)
        scale = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "✗✗✗ خطأ! ✗✗✗",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier
                .scale(scale)
                .rotate(rotation)
                .animatedGlow(glowColor = Color.Red)
        )
    }
}

@Composable
fun LevelCompleteCinematic(
    level: Int,
    score: Int,
    onNext: () -> Unit,
    onHome: () -> Unit
) {
    var scale by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00CED1).copy(alpha = 0.9f),
                        Color.Black
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
                .scale(scale)
                .clip(RoundedCornerShape(40.dp))
                .background(Color.Black.copy(alpha = 0.95f))
                .border(2.dp, Color.Cyan, RoundedCornerShape(40.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 انتصار ساحق! 🏆",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.Cyan,
                style = TextStyle(
                    shadow = Shadow(color = Color.Cyan, blurRadius = 15f)
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "أكملت المستوى $level",
                fontSize = 24.sp,
                color = Color.White
            )
            
            Text(
                text = "نقاطك: $score",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button3D(
                    text = "المستوى التالي 🚀",
                    onClick = onNext,
                    color = Color.Cyan,
                    modifier = Modifier.weight(1f)
                )
                
                Button3D(
                    text = "🏠 الرئيسية",
                    onClick = onHome,
                    color = Color.Magenta,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun GameOverCinematic(
    level: Int,
    score: Int,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    var scale by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.Red.copy(alpha = 0.8f),
                        Color.Black
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
                .scale(scale)
                .clip(RoundedCornerShape(40.dp))
                .background(Color.Black.copy(alpha = 0.95f))
                .border(2.dp, Color.Red, RoundedCornerShape(40.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💀 هزيمة نكراء 💀",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.Red,
                style = TextStyle(
                    shadow = Shadow(color = Color.Red, blurRadius = 15f)
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "سقطت في المستوى $level",
                fontSize = 24.sp,
                color = Color.White
            )
            
            Text(
                text = "مجموع نقاطك: $score",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button3D(
                    text = "🔄 إعادة المحاولة",
                    onClick = onRestart,
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
                
                Button3D(
                    text = "🏠 الرئيسية",
                    onClick = onHome,
                    color = Color.Magenta,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val size: Int,
    val speed: Float,
    val color: Color
)

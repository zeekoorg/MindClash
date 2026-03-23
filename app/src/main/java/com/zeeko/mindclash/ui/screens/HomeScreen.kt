package com.zeeko.mindclash.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.repository.UserProgressRepository
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onNavigateToGame: (Int) -> Unit) {
    val context = LocalContext.current
    val progressRepo = remember { UserProgressRepository(context) }
    
    var unlockedLevel by remember { mutableIntStateOf(progressRepo.getUnlockedLevel()) }
    val totalLevels = 50
    var rotationAngle by remember { mutableStateOf(0f) }
    var starScale by remember { mutableStateOf(1f) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // تأثيرات متحركة
    LaunchedEffect(Unit) {
        unlockedLevel = progressRepo.getUnlockedLevel()
        val targetIndex = totalLevels - unlockedLevel
        if (targetIndex >= 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(targetIndex)
            }
        }
        while (true) {
            rotationAngle = (rotationAngle + 1f) % 360f
            starScale = 1f + (kotlin.math.sin(System.currentTimeMillis() / 500.0).toFloat() * 0.1f)
            delay(50)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cosmicGradient())
    ) {
        // طبقة النجوم المتألقة
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(150) { i ->
                val x = (i * 131) % size.width
                val y = (i * 253) % size.height
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f + (kotlin.math.sin(rotationAngle + i).toFloat() * 0.2f)),
                    radius = 2f + (i % 3).toFloat(),
                    center = Offset(x, y)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // الهيدر الكوني
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GlassCrystal, Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "صراع العقول",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = goldGradient(),
                        shadow = Shadow(
                            color = LiquidGold.copy(alpha = 0.6f),
                            blurRadius = 25f,
                            offset = Offset(2f, 2f)
                        )
                    ),
                    modifier = Modifier.scale(starScale)
                )
            }

            // مسار المستويات المتعرج
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 50.dp, horizontal = 30.dp),
                verticalArrangement = Arrangement.spacedBy(45.dp)
            ) {
                itemsIndexed((1..totalLevels).reversed().toList()) { index, levelNum ->
                    val isUnlocked = levelNum <= unlockedLevel
                    val isCurrent = levelNum == unlockedLevel
                    
                    val alignment = when (levelNum % 3) {
                        0 -> Alignment.CenterStart
                        1 -> Alignment.Center
                        else -> Alignment.CenterEnd
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentAlignment = alignment
                    ) {
                        CosmicLevelNode(
                            levelNumber = levelNum,
                            isUnlocked = isUnlocked,
                            isCurrent = isCurrent,
                            index = index,
                            onClick = { if (isUnlocked) onNavigateToGame(levelNum) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CosmicLevelNode(
    levelNumber: Int,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    index: Int,
    onClick: () -> Unit
) {
    var pulseScale by remember { mutableStateOf(1f) }
    var glowAlpha by remember { mutableStateOf(0.3f) }
    var rotation by remember { mutableStateOf(0f) }
    
    val infiniteTransition = rememberInfiniteTransition()
    pulseScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrent) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    ).value
    
    glowAlpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    ).value
    
    LaunchedEffect(isCurrent) {
        if (isCurrent) {
            rotation = 360f
            delay(500)
            rotation = 0f
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.rotate(rotation)
    ) {
        Box(
            modifier = Modifier
                .size(95.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) Brush.radialGradient(
                        colors = listOf(GlassCyan, GlassPurple)
                    ) else Brush.radialGradient(
                        colors = listOf(Color.DarkGray, Color.Gray)
                    )
                )
                .border(
                    width = if (isCurrent) 4.dp else 2.dp,
                    color = if (isUnlocked) NeonCyan else Color.Gray,
                    shape = CircleShape
                )
                .clickable(enabled = isUnlocked) { onClick() }
                .drawWithContent {
                    drawContent()
                    if (isCurrent) {
                        drawCircle(
                            color = NeonCyan.copy(alpha = glowAlpha),
                            radius = size.minDimension * 0.6f,
                            blendMode = BlendMode.Screen
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                !isUnlocked -> Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
                isCurrent -> Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = LiquidGold,
                    modifier = Modifier.size(50.dp)
                )
                else -> Icon(
                    Icons.Filled.Star,
                    contentDescription = "Completed",
                    tint = LiquidGold,
                    modifier = Modifier.size(45.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "مستوى $levelNumber",
            color = if (isUnlocked) TextSilver else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            style = if (isCurrent) androidx.compose.ui.text.TextStyle(
                shadow = Shadow(color = NeonCyan, blurRadius = 15f)
            ) else androidx.compose.ui.text.TextStyle.Default
        )
    }
}

package com.zeeko.mindclash.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.repository.UserProgressRepository
import com.zeeko.mindclash.ui.theme.*

@Composable
fun HomeScreen(onNavigateToGame: (Int) -> Unit) {
    val context = LocalContext.current
    // جلب المستوى المفتوح حالياً من الذاكرة
    val progressRepo = remember { UserProgressRepository(context) }
    var unlockedLevel by remember { mutableIntStateOf(progressRepo.getUnlockedLevel()) }

    // تحديث الواجهة إذا عاد اللاعب من اللعبة بعد الفوز
    LaunchedEffect(Unit) {
        unlockedLevel = progressRepo.getUnlockedLevel()
    }

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))))
    ) {
        // رأس الشاشة (Header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "خريطة العقول",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(color = NeonBlue, blurRadius = 20f)
                )
            )
        }

        // مسار المستويات المتعرج (Zigzag Path)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 40.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            items(10) { index -> // 10 مستويات مبدئياً
                val levelNum = 10 - index // لعرض المستوى الأول بالأسفل (مثل كاندي كراش)
                val isUnlocked = levelNum <= unlockedLevel
                val isCurrent = levelNum == unlockedLevel

                // جعل المسار متعرجاً يميناً ويساراً
                val alignment = when (levelNum % 3) {
                    0 -> Alignment.CenterStart
                    1 -> Alignment.Center
                    else -> Alignment.CenterEnd
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    contentAlignment = alignment
                ) {
                    MapNode(
                        levelNumber = levelNum,
                        isUnlocked = isUnlocked,
                        isCurrent = isCurrent,
                        onClick = {
                            if (isUnlocked) onNavigateToGame(levelNum)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MapNode(levelNumber: Int, isUnlocked: Boolean, isCurrent: Boolean, onClick: () -> Unit) {
    // أنميشن النبض للمستوى الحالي فقط
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrent) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_scale"
    )

    val nodeColor = if (isUnlocked) NeonBlue else Color.DarkGray
    val glowColor = if (isCurrent) NeonPink else (if (isUnlocked) NeonBlue.copy(alpha = 0.5f) else Color.Transparent)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(if (isUnlocked) Color(0xFF131A2A) else Color(0xFF0A0E17))
                .border(
                    width = if (isCurrent) 4.dp else 2.dp,
                    color = nodeColor,
                    shape = CircleShape
                )
                .clickable(enabled = isUnlocked) { onClick() }
                // تأثير التوهج الخارجي
                .background(Brush.radialGradient(listOf(glowColor, Color.Transparent))),
            contentAlignment = Alignment.Center
        ) {
            if (!isUnlocked) {
                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(30.dp))
            } else if (isCurrent) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = NeonPink, modifier = Modifier.size(40.dp))
            } else {
                Icon(Icons.Filled.Star, contentDescription = "Completed", tint = Gold, modifier = Modifier.size(35.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "مستوى $levelNumber",
            color = if (isUnlocked) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

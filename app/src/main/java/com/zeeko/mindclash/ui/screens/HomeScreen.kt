package com.zeeko.mindclash.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.repository.UserProgressRepository
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onNavigateToGame: (Int) -> Unit) {
    val context = LocalContext.current
    val progressRepo = remember { UserProgressRepository(context) }
    
    // جلب أعلى مستوى وصل له اللاعب
    var unlockedLevel by remember { mutableIntStateOf(progressRepo.getUnlockedLevel()) }
    val totalLevels = 50 // افترضنا أن اللعبة تحتوي على 50 مستوى مبدئياً

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // تحديث الواجهة والنزول التلقائي للمستوى الحالي
    LaunchedEffect(Unit) {
        unlockedLevel = progressRepo.getUnlockedLevel()
        // تمرير سلس (Scroll) إلى المستوى المفتوح لكي لا يتعب اللاعب في البحث
        val targetIndex = totalLevels - unlockedLevel
        if (targetIndex >= 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ObsidianBlack, MidnightBlue)))
    ) {
        // --- 👑 رأس الشاشة (الهيدر الزجاجي) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassWhite)
                .padding(vertical = 25.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "صراع العقول",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = LiquidGold,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(color = LiquidGold, blurRadius = 25f)
                )
            )
        }

        // --- 🌌 مسار المستويات المتعرج ---
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 50.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp) // المسافة بين المستويات
        ) {
            items(totalLevels) { index ->
                // نعكس الترتيب ليكون المستوى الأول بالأسفل (مثل ألعاب الساغا)
                val levelNum = totalLevels - index 
                val isUnlocked = levelNum <= unlockedLevel
                val isCurrent = levelNum == unlockedLevel

                // خوارزمية التعرج (يمين، وسط، يسار)
                val alignment = when (levelNum % 3) {
                    0 -> Alignment.CenterStart
                    1 -> Alignment.Center
                    else -> Alignment.CenterEnd
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp),
                    contentAlignment = alignment
                ) {
                    LevelNode(
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

// --- ✨ أيقونة المستوى (العقدة) ---
@Composable
fun LevelNode(levelNumber: Int, isUnlocked: Boolean, isCurrent: Boolean, onClick: () -> Unit) {
    // أنميشن النبض (Pulse) للمستوى الحالي فقط
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrent) 1.2f else 1f, // تكبير بنسبة 20%
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_scale"
    )

    // تحديد ألوان النود حسب حالتها
    val nodeBorderColor = if (isUnlocked) NeonCyan else Color.DarkGray
    val nodeBgColor = if (isUnlocked) MidnightBlue else ObsidianBlack
    val glowColor = if (isCurrent) NeonCyan else Color.Transparent

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(85.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(nodeBgColor)
                .border(
                    width = if (isCurrent) 4.dp else 2.dp,
                    color = nodeBorderColor,
                    shape = CircleShape
                )
                .clickable(enabled = isUnlocked) { onClick() }
                // تأثير التوهج الخلفي (Glow)
                .background(Brush.radialGradient(listOf(glowColor.copy(alpha = 0.5f), Color.Transparent))),
            contentAlignment = Alignment.Center
        ) {
            if (!isUnlocked) {
                // مستوى مغلق
                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(35.dp))
            } else if (isCurrent) {
                // المستوى الحالي (جاهز للعب)
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = LiquidGold, modifier = Modifier.size(45.dp))
            } else {
                // مستوى تم الفوز به مسبقاً
                Icon(Icons.Filled.Star, contentDescription = "Completed", tint = LiquidGold, modifier = Modifier.size(40.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // رقم المستوى أسفل الدائرة
        Text(
            text = "مستوى $levelNumber",
            color = if (isUnlocked) TextSilver else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            style = if (isCurrent) androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(color = NeonCyan, blurRadius = 15f)
            ) else androidx.compose.ui.text.TextStyle.Default
        )
    }
}


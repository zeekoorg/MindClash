package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.R
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

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // متغير لتتبع عدد ضغطات زر الرجوع
    var backPressedOnce by remember { mutableStateOf(false) }

    // التحكم في زر الرجوع (الضغط مرتين للخروج)
    BackHandler {
        if (backPressedOnce) {
            (context as? Activity)?.finish() // الخروج من التطبيق
        } else {
            backPressedOnce = true
            Toast.makeText(context, "اضغط مرة أخرى للخروج", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                delay(2000) // بعد ثانيتين يتم تصفير العداد
                backPressedOnce = false
            }
        }
    }
    
    // التمرير التلقائي للمستوى المفتوح
    LaunchedEffect(Unit) {
        unlockedLevel = progressRepo.getUnlockedLevel()
        val targetIndex = totalLevels - unlockedLevel
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // خلفية الخريطة الجديدة (يجب إضافة صورة bg_home.png في مجلد drawable)
        Image(
            painter = painterResource(id = R.drawable.bg_home), 
            contentDescription = "Home Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // مسار المستويات (الخريطة) أخذت الشاشة بالكامل بعد حذف الهيدر
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 50.dp, horizontal = 30.dp),
            verticalArrangement = Arrangement.spacedBy(45.dp)
        ) {
            itemsIndexed((1..totalLevels).reversed().toList()) { index, levelNum ->
                val isUnlocked = levelNum <= unlockedLevel
                val isCurrent = levelNum == unlockedLevel
                
                // التوزيع المتعرج للمستويات
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
                    LevelNodeCustom(
                        levelNumber = levelNum,
                        isUnlocked = isUnlocked,
                        isCurrent = isCurrent,
                        onClick = { if (isUnlocked) onNavigateToGame(levelNum) }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelNodeCustom(
    levelNumber: Int,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    var pulseScale by remember { mutableStateOf(1f) }
    
    // تأثير النبض للمستوى المفتوح حالياً فقط
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    pulseScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrent) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    ).value
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = isUnlocked) { onClick() }
    ) {
        
        // الأيقونة المخصصة حسب حالة المستوى
        val iconRes = when {
            !isUnlocked -> R.drawable.ic_level_locked
            isCurrent -> R.drawable.ic_level_current
            else -> R.drawable.ic_level_completed
        }

        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Level $levelNumber",
            modifier = Modifier
                .size(90.dp)
                .scale(if (isCurrent) pulseScale else 1f)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // رقم المستوى تحت الأيقونة
        Text(
            text = "مستوى $levelNumber",
            color = if (isUnlocked) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            style = if (isCurrent) androidx.compose.ui.text.TextStyle(
                shadow = Shadow(color = NeonCyan, blurRadius = 15f)
            ) else androidx.compose.ui.text.TextStyle.Default
        )
    }
}

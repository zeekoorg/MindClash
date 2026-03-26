package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    
    // --- إعدادات الخصوصية والذاكرة ---
    val sharedPreferences = context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE)
    var hasAgreedToPrivacy by remember { mutableStateOf(sharedPreferences.getBoolean("PrivacyAgreed", false)) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    // 🔗 رابط سياسة الخصوصية (قم بتعديله لاحقاً)
    val privacyPolicyUrl = "https://www.google.com" 

    var unlockedLevel by remember { mutableIntStateOf(progressRepo.getUnlockedLevel()) }
    val totalLevels = 50

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var backPressedOnce by remember { mutableStateOf(false) }

    BackHandler {
        if (backPressedOnce) {
            (context as? Activity)?.finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, "اضغط مرة أخرى للخروج", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                delay(2000)
                backPressedOnce = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        unlockedLevel = progressRepo.getUnlockedLevel()
        val targetIndex = totalLevels - unlockedLevel
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        Image(
            painter = painterResource(id = R.drawable.bg_home), 
            contentDescription = "Home Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 100.dp, bottom = 50.dp, start = 30.dp, end = 30.dp), 
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

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = alignment) {
                    LevelNodeCustom(
                        levelNumber = levelNum,
                        isUnlocked = isUnlocked,
                        isCurrent = isCurrent,
                        onClick = { if (isUnlocked) onNavigateToGame(levelNum) }
                    )
                }
            }
        }

        // --- ⚙️ زر الإعدادات (الترس) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, end = 20.dp) // المسافة من الأعلى واليمين
                .align(Alignment.TopEnd)
        ) {
            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(VoidBlack.copy(alpha = 0.8f))
                    .border(2.dp, NeonCyan, CircleShape)
                    .size(55.dp) // حجم الزر
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = NeonCyan,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }

    // ==========================================
    // --- النوافذ المنبثقة (Dialogs) يجب أن تكون خارج الـ Box الرئيسي لتغطي الشاشة ---
    // ==========================================

    // --- 🛡️ نافذة سياسة الخصوصية الإجبارية (عند أول تشغيل) ---
    if (!hasAgreedToPrivacy) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(30.dp)).background(VoidBlack).border(2.dp, NeonCyan, RoundedCornerShape(30.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "سياسة الخصوصية", fontSize = 28.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "مرحباً بك في صراع العقول!\nيرجى قراءة والموافقة على سياسة الخصوصية الخاصة بنا لنتمكن من تقديم أفضل تجربة لعب لك وحفظ تقدمك.", 
                    fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center, lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
                
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan), modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(20.dp)
                ) { Text("قراءة سياسة الخصوصية 📖", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                
                Spacer(modifier = Modifier.height(15.dp))
                
                Button(
                    onClick = {
                        sharedPreferences.edit().putBoolean("PrivacyAgreed", true).apply()
                        hasAgreedToPrivacy = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)), border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan), modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(20.dp)
                ) { Text("موافق ومتابعة ✅", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            }
        }
    }

    // --- ℹ️ نافذة الإعدادات ومعلومات التطبيق (عند النقر على الترس) ---
    if (showSettingsDialog) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(30.dp)).background(VoidBlack).border(2.dp, LiquidGold, RoundedCornerShape(30.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                
                Text(text = "إعدادات اللعبة", fontSize = 28.sp, fontWeight = FontWeight.Black, color = LiquidGold, style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 15f)))
                Spacer(modifier = Modifier.height(25.dp))
                
                // معلومات التطبيق
                Text(text = "صراع العقول", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "الإصدار 1.0.0", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(20.dp))
                
                // هوية المطور (ZEEKO ORG)
                Text(text = "تطوير بواسطة:", fontSize = 16.sp, color = Color.LightGray)
                Text(text = "ZEEKO ORG", fontSize = 32.sp, fontWeight = FontWeight.Black, color = NeonCyan, style = TextStyle(shadow = Shadow(color = NeonCyan, blurRadius = 15f)))
                
                Spacer(modifier = Modifier.height(35.dp))
                
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGold), modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(20.dp)
                ) { Text("سياسة الخصوصية 📜", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                
                Spacer(modifier = Modifier.height(15.dp))
                
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidGold.copy(alpha = 0.2f)), border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGold), modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(20.dp)
                ) { Text("إغلاق", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
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
        val iconRes = when {
            !isUnlocked -> R.drawable.ic_level_locked
            isCurrent -> R.drawable.ic_level_current
            else -> R.drawable.ic_level_completed
        }

        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Level $levelNumber",
            modifier = Modifier.size(90.dp).scale(if (isCurrent) pulseScale else 1f)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
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

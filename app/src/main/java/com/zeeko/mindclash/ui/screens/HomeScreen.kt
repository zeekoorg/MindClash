package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zeeko.mindclash.AudioPlayer
import com.zeeko.mindclash.R
import com.zeeko.mindclash.repository.UserProgressRepository
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToGame: (Int) -> Unit,
    onNavigateToStore: () -> Unit,
    onNavigateToSurvival: () -> Unit,
    onNavigateToWheel: () -> Unit // ✨ تمت إضافة توجيه عجلة الحظ هنا
) {
    val context = LocalContext.current
    val progressRepo = remember { UserProgressRepository(context) }
    val sharedPreferences = context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE)
    
    var hasAgreedToPrivacy by remember { mutableStateOf(sharedPreferences.getBoolean("PrivacyAgreed", false)) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    var musicEnabled by remember { mutableStateOf(AudioPlayer.isMusicEnabled) }
    var sfxEnabled by remember { mutableStateOf(AudioPlayer.isSfxEnabled) }
    
    val privacyPolicyUrl = "https://www.google.com" 

    var unlockedLevel by remember { mutableIntStateOf(progressRepo.getUnlockedLevel()) }
    val totalLevels = 50

    // ✨ تحدي النجاة يظل مقفولاً حتى المستوى 5، أما المتجر أصبح مفتوحاً دائماً
    val isSurvivalUnlocked = unlockedLevel >= 5

    // جلب الرصيد اللحظي للقلوب والعملات
    var currentCoins by remember { mutableIntStateOf(sharedPreferences.getInt("Coins", 0)) }
    var currentLives by remember { mutableIntStateOf(sharedPreferences.getInt("Lives", 5)) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var backPressedOnce by remember { mutableStateOf(false) }

    val currentDayIndex = (System.currentTimeMillis() / 86400000L).toInt()
    val lastClaimDay = sharedPreferences.getInt("LastClaimDay", 0)
    var currentStreak by remember { mutableIntStateOf(sharedPreferences.getInt("CurrentStreak", 0)) }
    var showDailyRewardDialog by remember { mutableStateOf(false) }

    // تحديث الرصيد والمستوى كل مرة تظهر فيها الشاشة
    LaunchedEffect(Unit) {
        unlockedLevel = progressRepo.getUnlockedLevel()
        currentCoins = sharedPreferences.getInt("Coins", 0)
        currentLives = sharedPreferences.getInt("Lives", 5)
        
        val targetIndex = totalLevels - unlockedLevel
        if (targetIndex >= 0) listState.animateScrollToItem(targetIndex)

        if (currentDayIndex > lastClaimDay) {
            if (currentDayIndex > lastClaimDay + 1 && lastClaimDay != 0) {
                currentStreak = 0
                sharedPreferences.edit().putInt("CurrentStreak", 0).apply()
            }
            showDailyRewardDialog = true 
        }
    }

    BackHandler {
        if (backPressedOnce) {
            (context as? Activity)?.finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, "اضغط مرة أخرى للخروج", Toast.LENGTH_SHORT).show()
            coroutineScope.launch { delay(2000); backPressedOnce = false }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.bg_home), contentDescription = "Home", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 120.dp, bottom = 140.dp, start = 30.dp, end = 30.dp), 
            verticalArrangement = Arrangement.spacedBy(45.dp)
        ) {
            itemsIndexed((1..totalLevels).reversed().toList()) { index, levelNum ->
                val isUnlocked = levelNum <= unlockedLevel
                val isCurrent = levelNum == unlockedLevel
                val alignment = when (levelNum % 3) { 0 -> Alignment.CenterStart; 1 -> Alignment.Center; else -> Alignment.CenterEnd }

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = alignment) {
                    LevelNodeCustom(levelNumber = levelNum, isUnlocked = isUnlocked, isCurrent = isCurrent) {
                        AudioPlayer.playClick()
                        if (isUnlocked) onNavigateToGame(levelNum)
                    }
                }
            }
        }

        // --- الشريط العلوي المُرتب ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 15.dp, end = 15.dp).align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // مجموعة الأزرار (الإعدادات والمتجر)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // زر الإعدادات
                IconButton(
                    onClick = { AudioPlayer.playClick(); showSettingsDialog = true },
                    modifier = Modifier.clip(CircleShape).background(VoidBlack.copy(alpha = 0.8f)).border(2.dp, NeonCyan, CircleShape).size(45.dp)
                ) { Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings", tint = NeonCyan, modifier = Modifier.size(24.dp)) }

                // زر المتجر (أصبح مفتوحاً للجميع) ✨
                IconButton(
                    onClick = { 
                        AudioPlayer.playClick()
                        onNavigateToStore()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(VoidBlack.copy(alpha = 0.8f))
                        .border(2.dp, LiquidGold, CircleShape)
                        .size(45.dp)
                ) { 
                    Text("🛒", fontSize = 20.sp) 
                }
            }

            // مجموعة الرصيد (عجلة الحظ، العملات، القلوب)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                
                // ✨ زر أيقونة عجلة الحظ الدوارة ✨
                val infiniteTransition = rememberInfiniteTransition(label = "wheel_spin")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing), // تدور كل 4 ثواني بنعومة
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "spin_animation"
                )

                IconButton(
                    onClick = { 
                        AudioPlayer.playClick()
                        onNavigateToWheel()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(VoidBlack.copy(alpha = 0.8f))
                        .border(2.dp, NeonCyan, CircleShape)
                        .size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.wheel_asset), // تأكد من اسم ملف صورتك هنا
                        contentDescription = "Wheel of Fortune",
                        modifier = Modifier.size(26.dp).rotate(rotation)
                    )
                }

                // صندوق العملات
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(VoidBlack.copy(alpha = 0.8f), RoundedCornerShape(20.dp)).border(1.dp, LiquidGold, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(text = "$currentCoins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Image(painter = painterResource(id = R.drawable.ic_coin_custom), contentDescription = "Coins", modifier = Modifier.size(20.dp))
                }
                
                // صندوق القلوب
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(VoidBlack.copy(alpha = 0.8f), RoundedCornerShape(20.dp)).border(1.dp, CrimsonRed, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(text = "$currentLives", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Image(painter = painterResource(id = R.drawable.ic_heart_custom), contentDescription = "Hearts", modifier = Modifier.size(20.dp))
                }
            }
        }

        // --- زر صراع الزمن ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(VoidBlack.copy(alpha = 0.95f))
                .border(2.dp, if (isSurvivalUnlocked) CrimsonRed else Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(25.dp))
                .clickable { 
                    AudioPlayer.playClick()
                    if (isSurvivalUnlocked) onNavigateToSurvival()
                    else Toast.makeText(context, "صراع الزمن يُفتح عند الوصول للمستوى 5 🔒", Toast.LENGTH_SHORT).show()
                }, 
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(if (isSurvivalUnlocked) "⏳" else "🔒", fontSize = 40.sp)
                Spacer(modifier = Modifier.width(15.dp))
                Column {
                    Text(
                        text = "صراع الزمن", 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Black, 
                        color = if (isSurvivalUnlocked) CrimsonRed else Color.Gray, 
                        style = TextStyle(shadow = Shadow(color = if (isSurvivalUnlocked) CrimsonRed else Color.Transparent, blurRadius = 15f))
                    )
                    
                    if (isSurvivalUnlocked) {
                        Text("تحدي النجاة اللانهائي!", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Text("يُفتح عند الوصول للمستوى 5", fontSize = 14.sp, color = CrimsonRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- نوافذ الحوار ---
    if (showDailyRewardDialog) {
        Dialog(
            onDismissRequest = { /* إجبار اللاعب على الضغط على زر الاستلام */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(VoidBlack).border(3.dp, LiquidGold, RoundedCornerShape(30.dp)).padding(20.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("المكافآت اليومية 🎁", fontSize = 28.sp, fontWeight = FontWeight.Black, color = LiquidGold, style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 10f)))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("ادخل كل يوم لتصل للجائزة الكبرى!", fontSize = 14.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(20.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val rewards = listOf(
                            Pair("يوم 1", Pair(50, 0)), Pair("يوم 2", Pair(100, 0)),
                            Pair("يوم 3", Pair(0, 1)), Pair("يوم 4", Pair(150, 0)),
                            Pair("يوم 5", Pair(0, 2)), Pair("يوم 6", Pair(200, 0))
                        )

                        rewards.forEachIndexed { index, reward ->
                            val isPast = index < currentStreak
                            val isToday = index == currentStreak
                            DailyRewardItem(
                                title = reward.first, coins = reward.second.first, lives = reward.second.second,
                                isPast = isPast, isToday = isToday, isEpic = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    DailyRewardItem(
                        title = "اليوم السابع (الكنز) 💎", coins = 500, lives = 3,
                        isPast = 6 < currentStreak, isToday = 6 == currentStreak, isEpic = true, modifier = Modifier.fillMaxWidth().height(80.dp)
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    Button(
                        onClick = {
                            AudioPlayer.playWin()
                            val coinsToGive = listOf(50, 100, 0, 150, 0, 200, 500)[currentStreak]
                            val livesToGive = listOf(0, 0, 1, 0, 2, 0, 3)[currentStreak]

                            sharedPreferences.edit().apply {
                                putInt("Coins", sharedPreferences.getInt("Coins", 0) + coinsToGive)
                                putInt("Lives", sharedPreferences.getInt("Lives", 5) + livesToGive)
                                putInt("LastClaimDay", currentDayIndex)
                                putInt("CurrentStreak", if (currentStreak >= 6) 0 else currentStreak + 1)
                            }.apply()
                            
                            currentCoins += coinsToGive
                            currentLives += livesToGive

                            Toast.makeText(context, "تم استلام المكافأة بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                            showDailyRewardDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidGold.copy(alpha = 0.2f)),
                        border = BorderStroke(2.dp, LiquidGold),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().height(55.dp)
                    ) {
                        Text("استلام المكافأة ✨", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (!hasAgreedToPrivacy) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(30.dp)).background(VoidBlack).border(2.dp, NeonCyan, RoundedCornerShape(30.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "سياسة الخصوصية", fontSize = 28.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "مرحباً بك في صراع العقول!\nيرجى قراءة والموافقة على سياسة الخصوصية الخاصة بنا لنتمكن من تقديم أفضل تجربة لعب لك وحفظ تقدمك.", fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center, lineHeight = 28.sp)
                Spacer(modifier = Modifier.height(30.dp))
                Button(onClick = { AudioPlayer.playClick(); context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan), modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(20.dp)) { Text("قراءة سياسة الخصوصية 📖", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(15.dp))
                Button(onClick = { AudioPlayer.playClick(); sharedPreferences.edit().putBoolean("PrivacyAgreed", true).apply(); hasAgreedToPrivacy = true }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)), border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan), modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(20.dp)) { Text("موافق ومتابعة ✅", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            }
        }
    }

    if (showSettingsDialog) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(30.dp)).background(VoidBlack).border(2.dp, LiquidGold, RoundedCornerShape(30.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "الإعدادات", fontSize = 28.sp, fontWeight = FontWeight.Black, color = LiquidGold, style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 15f)))
                Spacer(modifier = Modifier.height(25.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎵 الموسيقى", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Switch(checked = musicEnabled, onCheckedChange = { musicEnabled = it; AudioPlayer.setMusicEnabled(context, it); AudioPlayer.playClick() }, colors = SwitchDefaults.colors(checkedThumbColor = LiquidGold, checkedTrackColor = LiquidGold.copy(alpha = 0.5f)))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔊 المؤثرات الصوتية", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Switch(checked = sfxEnabled, onCheckedChange = { sfxEnabled = it; AudioPlayer.setSfxEnabled(context, it); AudioPlayer.playClick() }, colors = SwitchDefaults.colors(checkedThumbColor = LiquidGold, checkedTrackColor = LiquidGold.copy(alpha = 0.5f)))
                }
                Spacer(modifier = Modifier.height(30.dp))
                Text(text = "صراع العقول (1.0.0)", fontSize = 16.sp, color = Color.Gray)
                Text(text = "تطوير: ZEEKO ORG", fontSize = 20.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { AudioPlayer.playClick(); context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGold), modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(20.dp)) { Text("سياسة الخصوصية 📜", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(15.dp))
                Button(onClick = { AudioPlayer.playClick(); showSettingsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = LiquidGold.copy(alpha = 0.2f)), border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGold), modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(20.dp)) { Text("إغلاق", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            }
        }
    }
}

@Composable
fun DailyRewardItem(title: String, coins: Int, lives: Int, isPast: Boolean, isToday: Boolean, isEpic: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
    )

    val bgColor = when {
        isPast -> Color.DarkGray.copy(alpha = 0.5f)
        isToday -> LiquidGold.copy(alpha = 0.3f)
        else -> VoidBlack.copy(alpha = 0.8f)
    }
    val borderColor = when {
        isPast -> Color.Gray
        isToday -> LiquidGold
        isEpic -> NeonCyan
        else -> CrimsonRed.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .width(if (isEpic) 280.dp else 90.dp)
            .height(if (isEpic) 80.dp else 90.dp)
            .scale(if (isToday) scale else 1f)
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .border(if (isToday) 2.dp else 1.dp, borderColor, RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isPast) {
            Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color.Green, modifier = Modifier.size(40.dp))
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(title, color = if (isToday) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (coins > 0) {
                        Text("+$coins", color = LiquidGold, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Image(painter = painterResource(id = R.drawable.ic_coin_custom), contentDescription = "Coins", modifier = Modifier.size(16.dp))
                    }
                    if (coins > 0 && lives > 0) Spacer(modifier = Modifier.width(10.dp))
                    if (lives > 0) {
                        Text("+$lives", color = CrimsonRed, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Image(painter = painterResource(id = R.drawable.ic_heart_custom), contentDescription = "Lives", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LevelNodeCustom(levelNumber: Int, isUnlocked: Boolean, isCurrent: Boolean, onClick: () -> Unit) {
    var pulseScale by remember { mutableStateOf(1f) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    pulseScale = infiniteTransition.animateFloat(initialValue = 1f, targetValue = if (isCurrent) 1.15f else 1f, animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "pulse_anim").value
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(enabled = isUnlocked) { onClick() }) {
        val iconRes = when { !isUnlocked -> R.drawable.ic_level_locked; isCurrent -> R.drawable.ic_level_current; else -> R.drawable.ic_level_completed }
        Image(painter = painterResource(id = iconRes), contentDescription = "Level $levelNumber", modifier = Modifier.size(90.dp).scale(if (isCurrent) pulseScale else 1f))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "مستوى $levelNumber", color = if (isUnlocked) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 18.sp, style = if (isCurrent) androidx.compose.ui.text.TextStyle(shadow = Shadow(color = NeonCyan, blurRadius = 15f)) else androidx.compose.ui.text.TextStyle.Default)
    }
}

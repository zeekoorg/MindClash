package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zeeko.mindclash.AudioPlayer
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoreScreen(
    adManager: AdManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current as Activity
    val prefs = context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE)
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var currentCoins by remember { mutableIntStateOf(prefs.getInt("Coins", 0)) }
    var currentLives by remember { mutableIntStateOf(prefs.getInt("Lives", 5)) }

    fun addCoins(amount: Int) {
        currentCoins += amount
        prefs.edit().putInt("Coins", currentCoins).apply()
    }
    fun addLives(amount: Int) {
        currentLives += amount
        prefs.edit().putInt("Lives", currentLives).apply()
    }
    fun spendCoins(amount: Int): Boolean {
        if (currentCoins >= amount) {
            currentCoins -= amount
            prefs.edit().putInt("Coins", currentCoins).apply()
            return true
        }
        return false
    }

    val scope = rememberCoroutineScope()
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    
    var pendingWheelSpin by remember { mutableStateOf(false) }
    var pendingCoinsReward by remember { mutableStateOf(false) }

    // ✨ متغيرات شاشة الاحتفال (Win Dialog)
    var showWinDialog by remember { mutableStateOf(false) }
    var wonPrizeText by remember { mutableStateOf("") }
    var wonPrizeIcon by remember { mutableIntStateOf(R.drawable.ic_coin_custom) }
    var dialogColor by remember { mutableStateOf(LiquidGold) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
        label = "spin"
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (pendingWheelSpin) {
                    pendingWheelSpin = false
                    isSpinning = true
                    scope.launch {
                        delay(500) 
                        
                        // ✨ 1. تحديد الجائزة بالاحتمالات الدقيقة والصعبة
                        val prizeChance = (1..100).random()
                        val prizeType: Int
                        val targetStopAngle: Float
                        
                        when {
                            prizeChance <= 50 -> { // 50% فرصة
                                prizeType = 50
                                targetStopAngle = 0f 
                            }
                            prizeChance <= 83 -> { // 33% فرصة
                                prizeType = 2 
                                targetStopAngle = 270f 
                            }
                            prizeChance <= 98 -> { // 15% فرصة
                                prizeType = 150 
                                targetStopAngle = 180f 
                            }
                            else -> { // ✨ 2% فرصة فقط للجائزة الكبرى!
                                prizeType = 500 
                                targetStopAngle = 90f 
                            }
                        }

                        val fullSpins = 5 * 360f 
                        rotationAngle += (fullSpins + targetStopAngle)
                        
                        delay(5000) 
                        AudioPlayer.playWin()
                        
                        // ✨ 2. إعطاء الجائزة وتجهيز شاشة الاحتفال
                        when (prizeType) {
                            50 -> { 
                                addCoins(50)
                                wonPrizeText = "50 عملة ذهبية!"
                                wonPrizeIcon = R.drawable.ic_coin_custom
                                dialogColor = LiquidGold
                            }
                            2 -> { 
                                addLives(2)
                                wonPrizeText = "قلبين حياة إضافية! ❤️❤️"
                                wonPrizeIcon = R.drawable.ic_heart_custom
                                dialogColor = CrimsonRed
                            }
                            150 -> { 
                                addCoins(150)
                                wonPrizeText = "150 عملة ذهبية! 🌟"
                                wonPrizeIcon = R.drawable.ic_coin_custom
                                dialogColor = LiquidGold
                            }
                            500 -> { 
                                addCoins(500)
                                wonPrizeText = "الجائزة الكبرى!\n500 عملة ذهبية! 🎉💎"
                                wonPrizeIcon = R.drawable.ic_coin_custom
                                dialogColor = NeonCyan // نيون لتمييز الجائزة الكبرى
                            }
                        }
                        
                        rotationAngle %= 360f
                        isSpinning = false
                        showWinDialog = true // إظهار الدايلوج الفخم
                    }
                }
                
                if (pendingCoinsReward) {
                    pendingCoinsReward = false
                    addCoins(100)
                    wonPrizeText = "100 عملة ذهبية!"
                    wonPrizeIcon = R.drawable.ic_coin_custom
                    dialogColor = LiquidGold
                    showWinDialog = true // استخدام نفس الدايلوج للمتجر
                    AudioPlayer.playWin()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.bg_store), contentDescription = "Store Background", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 20.dp, end = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { AudioPlayer.playClick(); onNavigateBack() }, modifier = Modifier.clip(CircleShape).background(VoidBlack.copy(alpha = 0.8f)).border(2.dp, NeonCyan, CircleShape).size(50.dp)) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonCyan)
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(VoidBlack.copy(alpha = 0.8f), RoundedCornerShape(20.dp)).border(1.dp, CrimsonRed, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(text = "$currentLives", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(painter = painterResource(id = R.drawable.ic_heart_custom), contentDescription = "Hearts", modifier = Modifier.size(24.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(VoidBlack.copy(alpha = 0.8f), RoundedCornerShape(20.dp)).border(1.dp, LiquidGold, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(text = "$currentCoins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(painter = painterResource(id = R.drawable.ic_coin_custom), contentDescription = "Coins", modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                
                Text(text = "عجلة المصير 🎡", fontSize = 32.sp, fontWeight = FontWeight.Black, color = LiquidGold, style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 15f)))
                Text(text = "جرب حظك واربح جوائز ضخمة!", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 20.dp))

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.wheel_asset), 
                        contentDescription = "Wheel",
                        modifier = Modifier.fillMaxSize().padding(15.dp).rotate(animatedRotation) 
                    )
                    
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown, 
                        contentDescription = "Pointer", 
                        tint = CrimsonRed, 
                        modifier = Modifier.align(Alignment.TopCenter).offset(y = (-5).dp).size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isSpinning) return@Button
                        AudioPlayer.playClick()
                        adManager.showRewardedAd(
                            activity = context,
                            onRewardEarned = { pendingWheelSpin = true },
                            onAdFailed = { Toast.makeText(context, "الإعلان غير جاهز، حاول بعد قليل", Toast.LENGTH_SHORT).show() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidGold.copy(alpha = 0.2f)),
                    border = BorderStroke(2.dp, LiquidGold),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text("لف العجلة مجاناً (إعلان) 📺", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
                Divider(color = NeonCyan.copy(alpha = 0.5f), thickness = 2.dp)
                Spacer(modifier = Modifier.height(30.dp))

                Text(text = "السوق السوداء 🛒", fontSize = 32.sp, fontWeight = FontWeight.Black, color = CrimsonRed, style = TextStyle(shadow = Shadow(color = CrimsonRed, blurRadius = 15f)))
                Spacer(modifier = Modifier.height(20.dp))

                StoreItemCustom(
                    title = "خزنة العملات", description = "شاهد إعلان واحصل على 100 عملة", buttonText = "100 عملة", buttonColor = LiquidGold, buttonIcon = R.drawable.ic_coin_custom,
                    onClick = {
                        AudioPlayer.playClick()
                        adManager.showRewardedAd(context, onRewardEarned = { pendingCoinsReward = true }, onAdFailed = { Toast.makeText(context, "الإعلان غير جاهز", Toast.LENGTH_SHORT).show() })
                    }
                )
                
                StoreItemCustom(
                    title = "جرعة الحياة", description = "اشترِ 5 قلوب باستخدام عملاتك", buttonText = "بـ 200 عملة", buttonColor = CrimsonRed, buttonIcon = R.drawable.ic_heart_custom,
                    onClick = {
                        AudioPlayer.playClick()
                        if (spendCoins(200)) { 
                            addLives(5)
                            AudioPlayer.playWin()
                            wonPrizeText = "5 قلوب حياة إضافية! ❤️"
                            wonPrizeIcon = R.drawable.ic_heart_custom
                            dialogColor = CrimsonRed
                            showWinDialog = true
                        } else {
                            Toast.makeText(context, "رصيدك من العملات لا يكفي!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // ✨ شاشة الاحتفال (Win Dialog) مع الجزيئات المتساقطة
    if (showWinDialog) {
        Dialog(
            onDismissRequest = { /* لا نسمح بالإغلاق بالنقر بالخارج لضمان الضغط على موافق */ },
            properties = DialogProperties(usePlatformDefaultWidth = false) // لجعل الشاشة كاملة
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                
                // خلفية داكنة للتركيز على الجائزة
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)))
                
                // ✨ نظام الجزيئات المتساقطة (القلوب أو العملات)
                FallingParticlesAnimation(iconRes = wonPrizeIcon)

                // صندوق التهنئة
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(VoidBlack)
                        .border(3.dp, dialogColor, RoundedCornerShape(30.dp))
                        .padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ألف مبروك! 🎉", 
                        fontSize = 36.sp, 
                        fontWeight = FontWeight.Black, 
                        color = dialogColor,
                        style = TextStyle(shadow = Shadow(color = dialogColor, blurRadius = 15f))
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = "لقد ربحت:", fontSize = 20.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(15.dp))
                    
                    // أيقونة الجائزة كبيرة ونابضة
                    val infiniteTransition = rememberInfiniteTransition()
                    val iconScale by infiniteTransition.animateFloat(
                        initialValue = 0.9f, targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
                    )
                    Image(painter = painterResource(id = wonPrizeIcon), contentDescription = null, modifier = Modifier.size(100.dp).scale(iconScale))
                    
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = wonPrizeText, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    Button(
                        onClick = { AudioPlayer.playClick(); showWinDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = dialogColor.copy(alpha = 0.2f)),
                        border = BorderStroke(2.dp, dialogColor),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().height(55.dp)
                    ) {
                        Text("موافق واستلام الجائزة", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ✨ دالة برمجة تأثير الجزيئات المتساقطة سينمائياً
data class ParticleConfig(val xRatio: Float, val delay: Int, val duration: Int, val size: Dp)

@Composable
fun FallingParticlesAnimation(iconRes: Int) {
    // إنشاء 25 جزيء بأحجام وسرعات وأماكن عشوائية
    val config = remember { List(25) { ParticleConfig(Math.random().toFloat(), (0..2000).random(), (2500..4500).random(), (20..50).random().dp) } }
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight
        
        config.forEach { c ->
            val infiniteTransition = rememberInfiniteTransition()
            val yOffset by infiniteTransition.animateFloat(
                initialValue = -100f, 
                targetValue = height.value + 100f, 
                animationSpec = infiniteRepeatable(tween(c.duration, delayMillis = c.delay, easing = LinearEasing)), label = "falling_y"
            )
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f, 
                targetValue = 360f, 
                animationSpec = infiniteRepeatable(tween(c.duration, easing = LinearEasing)), label = "falling_rot"
            )
            
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = width * c.xRatio, y = yOffset.dp)
                    .size(c.size)
                    .rotate(rotation)
                    .alpha(0.8f) // شفافية بسيطة لتبدو كالخلفية
            )
        }
    }
}

@Composable
fun StoreItemCustom(title: String, description: String, buttonText: String, buttonColor: Color, buttonIcon: Int, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).clip(RoundedCornerShape(20.dp)).background(VoidBlack.copy(alpha = 0.7f)).border(1.dp, buttonColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = description, fontSize = 14.sp, color = Color.Gray)
        }
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = buttonColor.copy(alpha = 0.2f)), border = BorderStroke(1.dp, buttonColor), shape = RoundedCornerShape(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Image(painter = painterResource(id = buttonIcon), contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zeeko.mindclash.AudioPlayer
import com.zeeko.mindclash.R
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WheelScreen(
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

    val scope = rememberCoroutineScope()
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var pendingWheelSpin by remember { mutableStateOf(false) }

    // متغيرات شاشة الفوز
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
                        
                        // تحديد الجائزة بالاحتمالات الدقيقة
                        val prizeChance = (1..100).random()
                        val prizeType: Int
                        val targetStopAngle: Float
                        
                        when {
                            prizeChance <= 50 -> { prizeType = 50; targetStopAngle = 0f }
                            prizeChance <= 83 -> { prizeType = 2; targetStopAngle = 270f }
                            prizeChance <= 98 -> { prizeType = 150; targetStopAngle = 180f }
                            else -> { prizeType = 500; targetStopAngle = 90f }
                        }

                        val fullSpins = 5 * 360f 
                        rotationAngle += (fullSpins + targetStopAngle)
                        
                        delay(5000) 
                        AudioPlayer.playWin()
                        
                        when (prizeType) {
                            50 -> { addCoins(50); wonPrizeText = "50 عملة ذهبية!"; wonPrizeIcon = R.drawable.ic_coin_custom; dialogColor = LiquidGold }
                            2 -> { addLives(2); wonPrizeText = "قلبين حياة إضافية! ❤️❤️"; wonPrizeIcon = R.drawable.ic_heart_custom; dialogColor = CrimsonRed }
                            150 -> { addCoins(150); wonPrizeText = "150 عملة ذهبية! 🌟"; wonPrizeIcon = R.drawable.ic_coin_custom; dialogColor = LiquidGold }
                            500 -> { addCoins(500); wonPrizeText = "الجائزة الكبرى!\n500 عملة ذهبية! 🎉💎"; wonPrizeIcon = R.drawable.ic_coin_custom; dialogColor = NeonCyan }
                        }
                        
                        rotationAngle %= 360f
                        isSpinning = false
                        showWinDialog = true 
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.bg_store), contentDescription = "Wheel Background", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

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
                
                Text(text = "عجلة المصير 🎡", fontSize = 36.sp, fontWeight = FontWeight.Black, color = LiquidGold, style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 15f)))
                Text(text = "جرب حظك واربح جوائز ضخمة!", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 30.dp))

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
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

                Spacer(modifier = Modifier.height(40.dp))

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
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
                    border = BorderStroke(2.dp, NeonCyan),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text("لف العجلة مجاناً (إعلان) 📺", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showWinDialog) {
        WinDialog(
            wonPrizeText = wonPrizeText, wonPrizeIcon = wonPrizeIcon, dialogColor = dialogColor,
            onDismiss = { AudioPlayer.playClick(); showWinDialog = false }
        )
    }
}

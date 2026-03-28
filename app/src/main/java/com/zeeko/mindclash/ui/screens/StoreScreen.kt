package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    var pendingCoinsReward by remember { mutableStateOf(false) }

    // متغيرات شاشة الفوز
    var showWinDialog by remember { mutableStateOf(false) }
    var wonPrizeText by remember { mutableStateOf("") }
    var wonPrizeIcon by remember { mutableIntStateOf(R.drawable.ic_coin_custom) }
    var dialogColor by remember { mutableStateOf(LiquidGold) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (pendingCoinsReward) {
                    pendingCoinsReward = false
                    addCoins(100)
                    wonPrizeText = "100 عملة ذهبية!"
                    wonPrizeIcon = R.drawable.ic_coin_custom
                    dialogColor = LiquidGold
                    showWinDialog = true 
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

            Spacer(modifier = Modifier.height(30.dp))

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                
                Text(text = "متجر العقول 🛒", fontSize = 36.sp, fontWeight = FontWeight.Black, color = CrimsonRed, style = TextStyle(shadow = Shadow(color = CrimsonRed, blurRadius = 15f)))
                Text(text = "تزود بالعملات واشترِ المزيد من القلوب", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 30.dp))

                StoreItemCustom(
                    title = "خزنة العملات", description = "شاهد إعلان واحصل على 100 عملة", buttonText = "100 عملة", buttonColor = LiquidGold, buttonIcon = R.drawable.ic_coin_custom,
                    onClick = {
                        AudioPlayer.playClick()
                        adManager.showRewardedAd(context, onRewardEarned = { pendingCoinsReward = true }, onAdFailed = { Toast.makeText(context, "الإعلان غير جاهز", Toast.LENGTH_SHORT).show() })
                    }
                )
                
                Spacer(modifier = Modifier.height(15.dp))

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

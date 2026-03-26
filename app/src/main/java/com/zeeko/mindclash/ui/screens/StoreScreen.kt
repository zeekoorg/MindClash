package com.zeeko.mindclash.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    // جلب الرصيد الحالي
    var currentCoins by remember { mutableIntStateOf(prefs.getInt("Coins", 0)) }
    var currentLives by remember { mutableIntStateOf(prefs.getInt("Lives", 5)) }

    // دوال تحديث الرصيد
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

    // متغيرات عجلة الحظ
    val scope = rememberCoroutineScope()
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
        label = "spin"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.bg_home), contentDescription = "Store Background", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            // --- الشريط العلوي (الرصيد وزر الرجوع) ---
            Row(modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 20.dp, end = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { AudioPlayer.playClick(); onNavigateBack() }, modifier = Modifier.clip(CircleShape).background(VoidBlack.copy(alpha = 0.8f)).border(2.dp, NeonCyan, CircleShape).size(50.dp)) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonCyan)
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(VoidBlack.copy(alpha = 0.8f), RoundedCornerShape(20.dp)).border(1.dp, CrimsonRed, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(text = "$currentLives", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("❤️", fontSize = 18.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(VoidBlack.copy(alpha = 0.8f), RoundedCornerShape(20.dp)).border(1.dp, LiquidGold, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(text = "$currentCoins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("🪙", fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                
                // --- قسم عجلة الحظ ---
                Text(text = "عجلة المصير 🎡", fontSize = 32.sp, fontWeight = FontWeight.Black, color = LiquidGold, style = TextStyle(shadow = Shadow(color = LiquidGold, blurRadius = 15f)))
                Text(text = "جرب حظك واربح جوائز ضخمة!", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 20.dp))

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
                    // خلفية العجلة (تخيلها دائرة مقسمة)
                    Box(modifier = Modifier.fillMaxSize().rotate(animatedRotation).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).border(4.dp, NeonCyan, CircleShape), contentAlignment = Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.logo_game), contentDescription = "Wheel Core", modifier = Modifier.size(100.dp).alpha(0.5f))
                        // يمكنك لاحقاً تصميم صورة لعجلة مقسمة لألوان وتضعها هنا بدلاً من الـ Box
                    }
                    
                    // المؤشر (السهم)
                    Icon(painter = painterResource(id = R.drawable.ic_status_wrong), contentDescription = "Pointer", tint = CrimsonRed, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-15).dp).size(40.dp).rotate(180f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isSpinning) return@Button
                        AudioPlayer.playClick()
                        adManager.showRewardedAd(
                            activity = context,
                            onRewardEarned = {
                                isSpinning = true
                                scope.launch {
                                    val randomSpins = (3..6).random() * 360f // لفات كاملة
                                    val randomStopAngle = (0..360).random().toFloat() // زاوية التوقف
                                    rotationAngle += randomSpins + randomStopAngle
                                    
                                    delay(4000) // انتظار انتهاء الأنيميشن
                                    AudioPlayer.playWin()
                                    
                                    // تحديد الجائزة برمجياً (بشكل عشوائي)
                                    val prize = (1..100).random()
                                    when {
                                        prize <= 50 -> { addCoins(50); Toast.makeText(context, "ربحت 50 عملة! 🪙", Toast.LENGTH_SHORT).show() }
                                        prize <= 80 -> { addLives(2); Toast.makeText(context, "ربحت قلبين! ❤️❤️", Toast.LENGTH_SHORT).show() }
                                        prize <= 95 -> { addCoins(150); Toast.makeText(context, "الجائزة الفضية: 150 عملة! 🌟", Toast.LENGTH_LONG).show() }
                                        else -> { addCoins(500); Toast.makeText(context, "الجائزة الكبرى: 500 عملة! 🎉💎", Toast.LENGTH_LONG).show() }
                                    }
                                    isSpinning = false
                                }
                            },
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

                // --- قسم السوق السوداء (المتجر) ---
                Text(text = "السوق السوداء 🛒", fontSize = 32.sp, fontWeight = FontWeight.Black, color = CrimsonRed, style = TextStyle(shadow = Shadow(color = CrimsonRed, blurRadius = 15f)))
                Spacer(modifier = Modifier.height(20.dp))

                StoreItemCustom(
                    title = "خزنة العملات", description = "شاهد إعلان واحصل على 100 عملة ذهبية", buttonText = "100 🪙", buttonColor = LiquidGold,
                    onClick = {
                        AudioPlayer.playClick()
                        adManager.showRewardedAd(context, onRewardEarned = { addCoins(100); AudioPlayer.playWin(); Toast.makeText(context, "تمت إضافة 100 عملة!", Toast.LENGTH_SHORT).show() }, onAdFailed = { Toast.makeText(context, "الإعلان غير جاهز", Toast.LENGTH_SHORT).show() })
                    }
                )
                
                StoreItemCustom(
                    title = "جرعة الحياة", description = "اشترِ 5 قلوب باستخدام عملاتك", buttonText = "شراء بـ 200 🪙", buttonColor = CrimsonRed,
                    onClick = {
                        AudioPlayer.playClick()
                        if (spendCoins(200)) { addLives(5); AudioPlayer.playWin(); Toast.makeText(context, "تم شراء 5 قلوب بنجاح!", Toast.LENGTH_SHORT).show() }
                        else Toast.makeText(context, "رصيدك من العملات لا يكفي!", Toast.LENGTH_SHORT).show()
                    }
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun StoreItemCustom(title: String, description: String, buttonText: String, buttonColor: Color, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).clip(RoundedCornerShape(20.dp)).background(VoidBlack.copy(alpha = 0.7f)).border(1.dp, buttonColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = description, fontSize = 14.sp, color = Color.Gray)
        }
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = buttonColor.copy(alpha = 0.2f)), border = BorderStroke(1.dp, buttonColor), shape = RoundedCornerShape(15.dp)) {
            Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}


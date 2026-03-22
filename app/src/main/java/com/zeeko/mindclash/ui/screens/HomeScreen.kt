package com.zeeko.mindclash.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.ui.theme.*

@Composable
fun HomeScreen(onNavigateToGame: (Int) -> Unit) {
    val levels = listOf("سهل", "متوسط", "صعب", "خبير", "أسطوري", "ملحمي", "خرافي", "كوني", "لا نهائي", "نهاية العقول")
    val levelColors = listOf(NeonGreen, Gold, NeonRed, NeonPurple, NeonBlue, NeonPink, NeonGreen, NeonPurple, NeonRed, Gold)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF020617)) // خلفية كونية مظلمة
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // العنوان بتوهج النيون
        Text(
            text = "صراع العقول",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = NeonBlue,
                    blurRadius = 20f
                )
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        // شبكة المستويات
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(levels.size) { index ->
                val levelNum = index + 1
                LevelButton(
                    title = levels[index],
                    levelNumber = levelNum,
                    neonColor = levelColors[index],
                    onClick = { onNavigateToGame(levelNum) }
                )
            }
        }
    }
}

@Composable
fun LevelButton(title: String, levelNumber: Int, neonColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f)) // تأثير الزجاج (Glassmorphism)
            .border(2.dp, neonColor.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "مستوى $levelNumber", color = neonColor, fontSize = 14.sp)
        }
    }
}

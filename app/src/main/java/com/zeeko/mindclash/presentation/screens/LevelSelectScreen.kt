package com.zeeko.mindclash.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeeko.mindclash.presentation.components.AnimatedBackground
import com.zeeko.mindclash.utils.LanguageManager

data class Level(
    val id: Int,
    val number: Int,
    val isUnlocked: Boolean,
    val stars: Int,
    val bestTime: String
)

@Composable
fun LevelSelectScreen(
    difficulty: Int,
    onLevelSelected: (Int) -> Unit,
    onBackPressed: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    // توليد 50 مستوى (10 مستويات × 5 أسئلة لكل مستوى)
    val levels = remember {
        (1..50).map { index ->
            Level(
                id = index,
                number = index,
                isUnlocked = index == 1,
                stars = if (index == 1) 3 else 0,
                bestTime = if (index == 1) "00:45" else "--:--"
            )
        }
    }
    
    AnimatedBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (difficulty) {
                                1 -> if (LanguageManager.isRTL()) "المستوى السهل" else "Easy Level"
                                2 -> if (LanguageManager.isRTL()) "المستوى المتوسط" else "Medium Level"
                                3 -> if (LanguageManager.isRTL()) "المستوى الصعب" else "Hard Level"
                                4 -> if (LanguageManager.isRTL()) "المستوى الخبير" else "Expert Level"
                                5 -> if (LanguageManager.isRTL()) "المستوى الأسطوري" else "Legendary Level"
                                6 -> if (LanguageManager.isRTL()) "المستوى الملحمي" else "Epic Level"
                                7 -> if (LanguageManager.isRTL()) "المستوى الخرافي" else "Mythic Level"
                                8 -> if (LanguageManager.isRTL()) "المستوى الكوني" else "Cosmic Level"
                                9 -> if (LanguageManager.isRTL()) "المستوى اللانهائي" else "Infinite Level"
                                10 -> if (LanguageManager.isRTL()) "نهاية العقول" else "Mind's End"
                                else -> ""
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isTablet) 5 else 3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(levels) { level ->
                    LevelItem(
                        level = level,
                        onClick = { if (level.isUnlocked) onLevelSelected(level.id) },
                        isTablet = isTablet
                    )
                }
            }
        }
    }
}

@Composable
fun LevelItem(
    level: Level,
    onClick: () -> Unit,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (level.isUnlocked)
                Color(0xFF6C63FF).copy(alpha = 0.2f)
            else
                Color.Gray.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = level.number.toString(),
                fontSize = if (isTablet) 32.sp else 24.sp,
                color = if (level.isUnlocked) Color(0xFF6C63FF) else Color.Gray,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            if (level.isUnlocked) {
                Row(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    repeat(level.stars) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                        )
                    }
                }
                
                Text(
                    text = level.bestTime,
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(if (isTablet) 32.dp else 24.dp)
                )
            }
        }
    }
}

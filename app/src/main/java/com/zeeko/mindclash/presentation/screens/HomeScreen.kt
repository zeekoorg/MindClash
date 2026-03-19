package com.zeeko.mindclash.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zeeko.mindclash.presentation.components.AnimatedBackground
import com.zeeko.mindclash.presentation.components.GlassmorphicCard
import com.zeeko.mindclash.presentation.viewmodels.HomeViewModel
import com.zeeko.mindclash.presentation.viewmodels.DifficultyLevel
import com.zeeko.mindclash.utils.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPlayClick: (Int) -> Unit,
    onNewSetClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    val infiniteTransition = rememberInfiniteTransition()
    val borderColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF40E0D0),
        targetValue = Color(0xFFFFFF00),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    AnimatedBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (LanguageManager.isRTL()) "صراع العقول" else "MindClash",
                            fontSize = if (isTablet) 28.sp else 22.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        // زر الإعدادات
                        IconButton(onClick = { /* TODO: Open settings */ }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // السيت الجديد الحصري - مع إطار وامض
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF8002706D)
                        ),
                        border = CardDefaults.outlinedCardBorder(borderColor)
                    ) {
                        Button(
                            onClick = onNewSetClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Icon(
                                Icons.Default.NewReleases,
                                contentDescription = null,
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (LanguageManager.isRTL()) "السيت الجديد الحصري 🤩" else "Exclusive New Set 🤩",
                                fontSize = if (isTablet) 20.sp else 16.sp,
                                color = Color.White
                            )
                        }
                    }
                    
                    // بطاقة إحصائيات المستخدم
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                icon = Icons.Default.Star,
                                value = uiState.userPoints.toString(),
                                label = if (LanguageManager.isRTL()) "النقاط" else "Points",
                                color = Color(0xFFFFD700)
                            )
                            
                            StatItem(
                                icon = Icons.Default.EmojiEvents,
                                value = "${uiState.highestLevelUnlocked}",
                                label = if (LanguageManager.isRTL()) "المستوى" else "Level",
                                color = Color(0xFF6C63FF)
                            )
                            
                            StatItem(
                                icon = Icons.Default.CheckCircle,
                                value = "${uiState.correctAnswers}/${uiState.questionsAnswered}",
                                label = if (LanguageManager.isRTL()) "الصح" else "Correct",
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // عنوان اختيار المستوى
                    Text(
                        text = if (LanguageManager.isRTL()) "اختر مستوى الصعوبة" else "Choose Difficulty Level",
                        fontSize = if (isTablet) 24.sp else 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // قائمة المستويات
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.difficultyLevels) { level ->
                            DifficultyLevelCard(
                                level = level,
                                onPlayClick = { onPlayClick(level.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}

@Composable
fun DifficultyLevelCard(
    level: DifficultyLevel,
    onPlayClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (level.isUnlocked)
                Color(level.color).copy(alpha = 0.2f)
            else
                Color.Gray.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = level.name,
                    fontSize = if (isTablet) 22.sp else 18.sp,
                    color = if (level.isUnlocked) Color(level.color) else Color.Gray,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = level.description,
                    fontSize = if (isTablet) 16.sp else 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "${level.questionCount} " + if (LanguageManager.isRTL()) "سؤال" else "questions",
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            
            if (level.isUnlocked) {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(level.color)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (LanguageManager.isRTL()) "العب" else "Play",
                        color = Color.White
                    )
                }
            } else {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

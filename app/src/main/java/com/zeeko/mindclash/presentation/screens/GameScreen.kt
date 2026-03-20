package com.zeeko.mindclash.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zeeko.mindclash.presentation.components.AnimatedBackground
import com.zeeko.mindclash.presentation.viewmodels.GameViewModel
import com.zeeko.mindclash.utils.LanguageManager
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    difficulty: Int,
    levelId: Int,
    onBackPressed: () -> Unit,
    onGameComplete: (Int) -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    val languageManager = LanguageManager.getInstance()
    
    LaunchedEffect(Unit) {
        viewModel.loadQuestions()
    }
    
    AnimatedBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.currentQuestion?.getCategory(languageManager.isRTL()) ?: "",
                            fontSize = if (isTablet) 24.sp else 18.sp
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
                    ),
                    actions = {
                        // النقاط
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF6C63FF).copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.userPoints.toString(),
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // عدد المحاولات
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF44336).copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "x${uiState.remainingLives}",
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // المؤقت
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.timeRemaining > 10)
                                    Color(0xFF4CAF50).copy(alpha = 0.3f)
                                else
                                    Color(0xFFF44336).copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (uiState.timeRemaining > 10)
                                        Color(0xFF4CAF50)
                                    else
                                        Color(0xFFF44336),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.timeRemaining.toString(),
                                    color = Color.White
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.currentQuestion != null && !uiState.showVictory) {
                    GameContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        isTablet = isTablet,
                        languageManager = languageManager
                    )
                }
                
                // شاشة النصر
                AnimatedVisibility(
                    visible = uiState.showVictory,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut(),
                    modifier = Modifier.matchParentSize()
                ) {
                    VictoryScreen(
                        points = uiState.levelPoints * uiState.correctAnswersCount,
                        correctAnswers = uiState.correctAnswersCount,
                        totalQuestions = 5,
                        onNextLevel = {
                            onGameComplete(uiState.userPoints)
                        },
                        languageManager = languageManager
                    )
                }
            }
        }
    }
}

@Composable
fun GameContent(
    uiState: GameUiState,
    viewModel: GameViewModel,
    isTablet: Boolean,
    languageManager: LanguageManager
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // مؤشر التقدم
        LinearProgressIndicator(
            progress = { ((uiState.currentQuestionIndex - 1) % 5) / 5f },
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTablet) 12.dp else 8.dp)
                .clip(CircleShape),
            color = Color(0xFF6C63FF),
            trackColor = Color.White.copy(alpha = 0.2f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (languageManager.isRTL())
                "السؤال ${(uiState.currentQuestionIndex - 1) % 5 + 1}/5"
            else
                "Question ${(uiState.currentQuestionIndex - 1) % 5 + 1}/5",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = if (isTablet) 18.sp else 14.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // السؤال
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E).copy(alpha = 0.8f)
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = uiState.currentQuestion?.getQuestion(languageManager.isRTL()) ?: "",
                fontSize = if (isTablet) 32.sp else 24.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // مربعات الإجابة
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            uiState.currentAnswer.forEachIndexed { index, char ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn()
                ) {
                    Card(
                        modifier = Modifier
                            .size(if (isTablet) 80.dp else 60.dp)
                            .padding(4.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (char != null)
                                Color(0xFF6C63FF)
                            else
                                Color(0xFF2A2A2A)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char?.toString() ?: "",
                                fontSize = if (isTablet) 32.sp else 24.sp,
                                color = if (char != null) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // لوحة المفاتيح العربية
        ArabicKeyboard(
            onCharClick = { viewModel.addChar(it) },
            onDeleteClick = { viewModel.deleteChar() },
            enabled = uiState.remainingLives > 0 && !uiState.isCompleted,
            isTablet = isTablet
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // أزرار المساعدة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // زر التلميح
            FloatingActionButton(
                onClick = { viewModel.showHintAd() },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier.size(if (isTablet) 80.dp else 56.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = Color.White)
                    Text(
                        text = if (languageManager.isRTL()) "تلميح" else "Hint",
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
            
            // زر كشف حرف
            FloatingActionButton(
                onClick = { /* TODO: Show ad to reveal letter */ },
                containerColor = Color(0xFF4CAF50),
                modifier = Modifier.size(if (isTablet) 80.dp else 56.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.TextFields, contentDescription = "Reveal", tint = Color.White)
                    Text(
                        text = if (languageManager.isRTL()) "كشف" else "Reveal",
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
            
            // زر 50:50
            FloatingActionButton(
                onClick = { /* TODO: Show ad for 50:50 */ },
                containerColor = Color(0xFFF44336),
                modifier = Modifier.size(if (isTablet) 80.dp else 56.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "50/50",
                        fontSize = if (isTablet) 18.sp else 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (languageManager.isRTL()) "فرصة" else "Chance",
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ArabicKeyboard(
    onCharClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    enabled: Boolean,
    isTablet: Boolean
) {
    val rows = listOf(
        listOf('ا', 'ب', 'ت', 'ث', 'ج', 'ح', 'خ'),
        listOf('د', 'ذ', 'ر', 'ز', 'س', 'ش', 'ص'),
        listOf('ض', 'ط', 'ظ', 'ع', 'غ', 'ف', 'ق'),
        listOf('ك', 'ل', 'م', 'ن', 'ه', 'و', 'ي')
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { char ->
                    KeyboardKey(
                        char = char,
                        onClick = { onCharClick(char) },
                        enabled = enabled,
                        isTablet = isTablet
                    )
                }
            }
        }
        
        // صف المسح
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            KeyboardKey(
                char = '⌫',
                onClick = onDeleteClick,
                enabled = enabled,
                isTablet = isTablet,
                isSpecial = true
            )
        }
    }
}

@Composable
fun KeyboardKey(
    char: Char,
    onClick: () -> Unit,
    enabled: Boolean,
    isTablet: Boolean,
    isSpecial: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(if (isTablet) 80.dp else 50.dp)
            .padding(2.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSpecial)
                Color(0xFFF44336).copy(alpha = 0.3f)
            else
                Color(0xFF6C63FF).copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = char.toString(),
            fontSize = if (isTablet) 28.sp else 18.sp,
            color = if (isSpecial) Color(0xFFF44336) else Color.White
        )
    }
}

@Composable
fun VictoryScreen(
    points: Int,
    correctAnswers: Int,
    totalQuestions: Int,
    onNextLevel: () -> Unit,
    languageManager: LanguageManager
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // أيقونة النصر
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(if (isTablet) 150.dp else 100.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6C63FF)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = if (languageManager.isRTL()) "أحسنت! 🎉" else "Victory! 🎉",
                        fontSize = if (isTablet) 48.sp else 32.sp,
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (languageManager.isRTL())
                            "لقد أجبت على $correctAnswers من $totalQuestions إجابات صحيحة"
                        else
                            "You answered $correctAnswers out of $totalQuestions correctly",
                        fontSize = if (isTablet) 20.sp else 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = if (languageManager.isRTL()) "لقد ربحت" else "You earned",
                        fontSize = if (isTablet) 18.sp else 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Text(
                        text = "$points " + if (languageManager.isRTL()) "نقطة" else "points",
                        fontSize = if (isTablet) 48.sp else 36.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isTablet) 70.dp else 50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (languageManager.isRTL()) "العودة للرئيسية" else "Back to Home",
                            fontSize = if (isTablet) 22.sp else 18.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

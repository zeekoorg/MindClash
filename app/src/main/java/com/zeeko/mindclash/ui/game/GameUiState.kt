package com.zeeko.mindclash.ui.game

import com.zeeko.mindclash.data.local.QuestionEntity

data class GameUiState(
    val isLoading: Boolean = true,
    val currentLevel: Int = 1,
    val score: Int = 0,
    val lives: Int = 3,
    val questions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val userAnswer: String = "",
    val timeLeft: Int = 60,
    val isGameOver: Boolean = false,
    val isLevelComplete: Boolean = false,
    val showCorrectAnimation: Boolean = false, // لتشغيل النيون الأخضر
    val showWrongAnimation: Boolean = false    // لتشغيل النيون الأحمر
) {
    // استخراج السؤال الحالي بسهولة
    val currentQuestion: QuestionEntity?
        get() = questions.getOrNull(currentQuestionIndex)
}

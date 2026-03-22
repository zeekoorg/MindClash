package com.zeeko.mindclash.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeeko.mindclash.repository.GameRepository
import com.zeeko.mindclash.repository.UserProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val progressRepository: UserProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    // تحميل المستوى
    fun loadLevel(level: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, currentLevel = level) }
            // تأخير بسيط لمحاكاة التحميل الجمالي
            delay(500)
            val questions = repository.getQuestionsForLevel(level)
            
            _state.update {
                it.copy(
                    isLoading = false,
                    questions = questions,
                    currentQuestionIndex = 0,
                    userAnswer = "",
                    isGameOver = it.lives <= 0,
                    isLevelComplete = false,
                    score = 0 // تصفير النقاط عند بدء مستوى جديد
                )
            }
            startTimer()
        }
    }

    // إدارة ضغطات الحروف
    fun onLetterClick(char: Char) {
        val currentState = _state.value
        val answer = currentState.currentQuestion?.answer ?: return

        if (currentState.userAnswer.length < answer.length) {
            val newAnswer = currentState.userAnswer + char
            _state.update { it.copy(userAnswer = newAnswer) }

            // التحقق التلقائي إذا اكتملت الحروف
            if (newAnswer.length == answer.length) {
                checkAnswer(newAnswer)
            }
        }
    }

    // مسح حرف
    fun onDeleteClick() {
        val currentAnswer = _state.value.userAnswer
        if (currentAnswer.isNotEmpty()) {
            _state.update { it.copy(userAnswer = currentAnswer.dropLast(1)) }
        }
    }

    // التحقق من الإجابة
    private fun checkAnswer(userAnswer: String) {
        timerJob?.cancel() // إيقاف العداد
        val currentState = _state.value
        
        // أخذنا نسخة محلية وآمنة من السؤال لتجنب خطأ Smart cast
        val currentQuestion = currentState.currentQuestion ?: return
        val correctAnswer = currentQuestion.answer

        if (userAnswer == correctAnswer) {
            // إجابة صحيحة
            val earnedPoints = currentQuestion.points
            _state.update { 
                it.copy(
                    score = it.score + earnedPoints,
                    showCorrectAnimation = true 
                ) 
            }
            // ننتظر الأنميشن ثم ننتقل للسؤال التالي
            viewModelScope.launch {
                delay(800)
                _state.update { it.copy(showCorrectAnimation = false) }
                nextQuestion()
            }
        } else {
            // إجابة خاطئة
            val newLives = currentState.lives - 1
            _state.update { 
                it.copy(
                    lives = newLives,
                    showWrongAnimation = true,
                    isGameOver = newLives <= 0
                ) 
            }
            viewModelScope.launch {
                delay(500)
                _state.update { it.copy(showWrongAnimation = false, userAnswer = "") }
                if (newLives > 0) startTimer()
            }
        }
    }

    // الانتقال للسؤال التالي
    private fun nextQuestion() {
        val currentState = _state.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _state.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    userAnswer = ""
                )
            }
            startTimer()
        } else {
            // 🚀 اكتمل المستوى! افتح المستوى التالي في الخريطة
            progressRepository.unlockNextLevel(currentState.currentLevel)
            _state.update { it.copy(isLevelComplete = true) }
        }
    }

    // استخدام تلميح الكشف (بعد مشاهدة إعلان المكافأة)
    fun revealLetter() {
        val currentState = _state.value
        val answer = currentState.currentQuestion?.answer ?: return
        val currentInput = currentState.userAnswer

        if (currentInput.length < answer.length) {
            val nextCorrectChar = answer[currentInput.length]
            onLetterClick(nextCorrectChar)
        }
    }

    // نظام العداد الدقيق
    private fun startTimer() {
        timerJob?.cancel()
        _state.update { it.copy(timeLeft = 60) }
        
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentLeft = _state.value.timeLeft
                if (currentLeft > 0) {
                    _state.update { it.copy(timeLeft = currentLeft - 1) }
                } else {
                    handleTimeout()
                    break
                }
            }
        }
    }

    private fun handleTimeout() {
        val newLives = _state.value.lives - 1
        _state.update { 
            it.copy(
                lives = newLives,
                isGameOver = newLives <= 0,
                userAnswer = ""
            )
        }
        if (newLives > 0) startTimer()
    }

    // إعادة ضبط اللعبة (عند الخسارة)
    fun resetGame() {
        _state.update { 
            GameUiState(score = 0, lives = 3) 
        }
        loadLevel(_state.value.currentLevel)
    }
}


package com.zeeko.mindclash.ui.game

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeeko.mindclash.data.local.QuestionEntity
import com.zeeko.mindclash.repository.GameRepository
import com.zeeko.mindclash.repository.UserProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameUiState(
    val isLoading: Boolean = true,
    val currentLevel: Int = 1,
    val score: Int = 0,
    val lives: Int = 5,
    val questions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val userAnswer: String = "",
    val isGameOver: Boolean = false,
    val isLevelComplete: Boolean = false,
    val showCorrectAnimation: Boolean = false,
    val showWrongAnimation: Boolean = false,
    val isHintVisible: Boolean = false
) {
    val currentQuestion: QuestionEntity? get() = questions.getOrNull(currentQuestionIndex)
}

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val progressRepository: UserProgressRepository,
    @ApplicationContext private val context: Context // ✨ إضافة الكونتكست للوصول للمتجر
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    // ✨ ربط مباشر بذاكرة المتجر وعجلة الحظ
    private val prefs = context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE)

    fun loadLevel(level: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, currentLevel = level) }
            
            var levelQuestions = repository.getQuestionsForLevel(level)
            var attempts = 0
            
            while (levelQuestions.isEmpty() && attempts < 5) {
                delay(500)
                levelQuestions = repository.getQuestionsForLevel(level)
                attempts++
            }

            val savedData = progressRepository.getSavedGameState(level)
            
            // ✨ جلب الرصيد الحقيقي من المتجر في اللحظة الحالية!
            val globalCoins = prefs.getInt("Coins", 0)
            val globalLives = prefs.getInt("Lives", 5)

            if (savedData != null) {
                _state.update {
                    it.copy(
                        isLoading = false, questions = levelQuestions, currentQuestionIndex = savedData.questionIndex,
                        score = globalCoins, lives = globalLives, userAnswer = savedData.currentAnswer,
                        isHintVisible = savedData.isHintVisible, isGameOver = globalLives <= 0, isLevelComplete = false
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false, questions = levelQuestions, currentQuestionIndex = 0,
                        score = globalCoins, lives = globalLives, userAnswer = "", isHintVisible = false,
                        isGameOver = globalLives <= 0, isLevelComplete = false
                    )
                }
            }
        }
    }

    private fun persistCurrentState() {
        val currentState = _state.value
        progressRepository.saveGameState(
            level = currentState.currentLevel, questionIndex = currentState.currentQuestionIndex,
            lives = currentState.lives, currentAnswer = currentState.userAnswer, isHintVisible = currentState.isHintVisible
        )
    }

    fun forceSaveState() {
        persistCurrentState()
    }

    fun onNativeKeyboardInput(text: String) {
        val answer = _state.value.currentQuestion?.answer ?: return
        val cleanText = text.replace("\n", "") 
        
        if (cleanText.length <= answer.length) {
            _state.update { it.copy(userAnswer = cleanText) }
            persistCurrentState()
            if (cleanText.length == answer.length) {
                checkAnswer(cleanText)
            }
        }
    }

    // ✨ دوال مركزية لتحديث الرصيد في كل مكان (اللعبة والمتجر معاً)
    private fun updateGlobalCoins(amount: Int) {
        val current = prefs.getInt("Coins", 0)
        val newTotal = current + amount
        prefs.edit().putInt("Coins", newTotal).apply()
        _state.update { it.copy(score = newTotal) }
    }

    private fun spendGlobalCoins(amount: Int): Boolean {
        val current = prefs.getInt("Coins", 0)
        if (current >= amount) {
            val newTotal = current - amount
            prefs.edit().putInt("Coins", newTotal).apply()
            _state.update { it.copy(score = newTotal) }
            return true
        }
        return false
    }

    private fun updateGlobalLives(amount: Int) {
        val current = prefs.getInt("Lives", 5)
        val newTotal = maxOf(0, current + amount)
        prefs.edit().putInt("Lives", newTotal).apply()
        _state.update { it.copy(lives = newTotal) }
    }

    // مكافأة الإعلانات داخل اللعبة
    fun rewardCoins(amount: Int) {
        updateGlobalCoins(amount)
    }

    fun rewardLives(amount: Int) {
        updateGlobalLives(amount)
        persistCurrentState()
        // إخفاء شاشة الخسارة فوراً إذا حصل على قلوب من الإعلان
        if (_state.value.lives > 0) {
            _state.update { it.copy(isGameOver = false) } 
        }
    }

    fun buyHint() {
        if (spendGlobalCoins(50)) {
            _state.update { it.copy(isHintVisible = true) }
            persistCurrentState()
        }
    }

    fun buyRevealLetter() {
        val answer = _state.value.currentQuestion?.answer ?: return
        val letterToReveal = answer.getOrNull(_state.value.userAnswer.length) ?: return
        if (spendGlobalCoins(50)) {
            val newAnswer = _state.value.userAnswer + letterToReveal
            onNativeKeyboardInput(newAnswer)
        }
    }

    fun showPermanentHint() {
        _state.update { it.copy(isHintVisible = true) }
        persistCurrentState()
    }

    fun revealLetterFree() {
        val answer = _state.value.currentQuestion?.answer ?: return
        val letterToReveal = answer.getOrNull(_state.value.userAnswer.length) ?: return
        val newAnswer = _state.value.userAnswer + letterToReveal
        onNativeKeyboardInput(newAnswer)
    }

    private fun checkAnswer(userAnswer: String) {
        val currentState = _state.value
        val currentQuestion = currentState.currentQuestion ?: return

        if (userAnswer == currentQuestion.answer) {
            updateGlobalCoins(5) // ✨ إضافة 5 عملات للإجابة الصحيحة وحفظها عالمياً
            _state.update { it.copy(showCorrectAnimation = true) }
            viewModelScope.launch {
                delay(800)
                _state.update { it.copy(showCorrectAnimation = false) }
                nextQuestion()
            }
        } else {
            updateGlobalLives(-1) // ✨ خصم قلب عالمياً وحفظه
            val newLives = _state.value.lives
            _state.update { it.copy(showWrongAnimation = true, isGameOver = newLives <= 0) }
            
            if (newLives <= 0) progressRepository.clearGameState() else persistCurrentState()
            
            viewModelScope.launch {
                delay(500)
                _state.update { it.copy(showWrongAnimation = false, userAnswer = "") }
            }
        }
    }

    private fun nextQuestion() {
        val currentState = _state.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _state.update { it.copy(currentQuestionIndex = it.currentQuestionIndex + 1, userAnswer = "", isHintVisible = false) }
            persistCurrentState() 
        } else {
            progressRepository.unlockNextLevel(currentState.currentLevel)
            progressRepository.clearGameState()
            _state.update { it.copy(isLevelComplete = true) }
        }
    }

    fun resetGame() {
        progressRepository.clearGameState()
        loadLevel(_state.value.currentLevel)
    }
}

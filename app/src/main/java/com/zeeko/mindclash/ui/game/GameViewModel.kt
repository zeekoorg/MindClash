package com.zeeko.mindclash.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeeko.mindclash.data.local.QuestionEntity
import com.zeeko.mindclash.repository.GameRepository
import com.zeeko.mindclash.repository.UserProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val lives: Int = 3,
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
    private val progressRepository: UserProgressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

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
            val globalCoins = progressRepository.getTotalCoins()

            if (savedData != null) {
                _state.update {
                    it.copy(
                        isLoading = false, questions = levelQuestions, currentQuestionIndex = savedData.questionIndex,
                        score = globalCoins, lives = savedData.lives, userAnswer = savedData.currentAnswer,
                        isHintVisible = savedData.isHintVisible, isGameOver = savedData.lives <= 0, isLevelComplete = false
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false, questions = levelQuestions, currentQuestionIndex = 0,
                        score = globalCoins, lives = 3, userAnswer = "", isHintVisible = false,
                        isGameOver = false, isLevelComplete = false
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

    fun onNativeKeyboardInput(text: String) {
        val answer = _state.value.currentQuestion?.answer ?: return
        // تنظيف النص من أي مسافات زائدة أو نزول سطر يفسد الإجابة
        val cleanText = text.replace("\n", "")
        
        if (cleanText.length <= answer.length) {
            _state.update { it.copy(userAnswer = cleanText) }
            persistCurrentState()
            if (cleanText.length == answer.length) {
                checkAnswer(cleanText)
            }
        }
    }

    fun rewardCoins(amount: Int) {
        progressRepository.addCoins(amount)
        _state.update { it.copy(score = progressRepository.getTotalCoins()) }
    }

    fun rewardLives(amount: Int) {
        val newLives = _state.value.lives + amount
        _state.update { it.copy(lives = newLives) }
        persistCurrentState()
    }

    fun buyHint() {
        if (progressRepository.spendCoins(50)) {
            _state.update { it.copy(score = progressRepository.getTotalCoins(), isHintVisible = true) }
            persistCurrentState()
        }
    }

    fun buyRevealLetter() {
        val answer = _state.value.currentQuestion?.answer ?: return
        val letterToReveal = answer.getOrNull(_state.value.userAnswer.length) ?: return
        if (progressRepository.spendCoins(50)) {
            _state.update { it.copy(score = progressRepository.getTotalCoins()) }
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

        // التحقق الدقيق من التطابق
        if (userAnswer == currentQuestion.answer) {
            progressRepository.addCoins(5) 
            _state.update { it.copy(score = progressRepository.getTotalCoins(), showCorrectAnimation = true) }
            viewModelScope.launch {
                delay(800)
                _state.update { it.copy(showCorrectAnimation = false) }
                nextQuestion()
            }
        } else {
            val newLives = currentState.lives - 1
            _state.update { it.copy(lives = newLives, showWrongAnimation = true, isGameOver = newLives <= 0) }
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

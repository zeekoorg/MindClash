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
    val currentQuestion: QuestionEntity?
        get() = questions.getOrNull(currentQuestionIndex)
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
            val levelQuestions = repository.getQuestionsForLevel(level)
            val savedData = progressRepository.getSavedGameState(level)

            if (savedData != null) {
                _state.update {
                    it.copy(
                        isLoading = false, questions = levelQuestions,
                        currentQuestionIndex = savedData.questionIndex, score = savedData.score,
                        lives = savedData.lives, userAnswer = savedData.currentAnswer,
                        isHintVisible = savedData.isHintVisible, isGameOver = savedData.lives <= 0,
                        isLevelComplete = false
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false, questions = levelQuestions, currentQuestionIndex = 0,
                        score = 0, lives = 3, userAnswer = "", isHintVisible = false,
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
            score = currentState.score, lives = currentState.lives, currentAnswer = currentState.userAnswer,
            isHintVisible = currentState.isHintVisible
        )
    }

    fun onLetterClick(char: Char) {
        val currentState = _state.value
        val answer = currentState.currentQuestion?.answer ?: return

        if (currentState.userAnswer.length < answer.length) {
            val newAnswer = currentState.userAnswer + char
            _state.update { it.copy(userAnswer = newAnswer) }
            persistCurrentState()

            if (newAnswer.length == answer.length) {
                checkAnswer(newAnswer)
            }
        }
    }

    fun onDeleteClick() {
        val currentAnswer = _state.value.userAnswer
        if (currentAnswer.isNotEmpty()) {
            _state.update { it.copy(userAnswer = currentAnswer.dropLast(1)) }
            persistCurrentState()
        }
    }

    fun showPermanentHint() {
        _state.update { it.copy(isHintVisible = true) }
        persistCurrentState()
    }

    fun revealLetter() {
        val currentState = _state.value
        val answer = currentState.currentQuestion?.answer ?: return
        if (currentState.userAnswer.length < answer.length) {
            onLetterClick(answer[currentState.userAnswer.length])
        }
    }

    private fun checkAnswer(userAnswer: String) {
        val currentState = _state.value
        val currentQuestion = currentState.currentQuestion ?: return

        if (userAnswer == currentQuestion.answer) {
            val earnedPoints = currentQuestion.points
            _state.update { it.copy(score = it.score + earnedPoints, showCorrectAnimation = true) }
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

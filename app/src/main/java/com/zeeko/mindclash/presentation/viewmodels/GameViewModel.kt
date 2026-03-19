package com.zeeko.mindclash.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeeko.mindclash.data.models.Question
import com.zeeko.mindclash.data.repository.QuestionRepository
import com.zeeko.mindclash.utils.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameUiState(
    val currentQuestion: Question? = null,
    val currentAnswer: Array<Char?> = arrayOf(),
    val userPoints: Int = 0,
    val levelPoints: Int = 0,
    val remainingLives: Int = 3,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val isCompleted: Boolean = false,
    val showVictory: Boolean = false,
    val showHintAd: Boolean = false,
    val showRewardedAd: Boolean = false,
    val pendingUrl: String = "",
    val correctAnswersCount: Int = 0,
    val wrongAnswersCount: Int = 0,
    val timeRemaining: Int = 60,
    val isTimerRunning: Boolean = true
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val languageManager: LanguageManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val difficulty: Int = savedStateHandle.get<Int>("difficulty") ?: 1
    private val levelId: Int = savedStateHandle.get<Int>("levelId") ?: 1
    private var questions: List<Question> = emptyList()
    private var currentQuestionIndex = 0
    
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    init {
        loadQuestions()
    }
    
    private fun loadQuestions() {
        viewModelScope.launch {
            questions = questionRepository.getQuestionsByDifficultyLevel(difficulty)
            if (questions.isNotEmpty()) {
                // نبدأ من السؤال المناسب حسب levelId
                val startIndex = (levelId - 1) * 5
                if (startIndex < questions.size) {
                    loadQuestion(startIndex)
                    startTimer()
                }
            }
        }
    }
    
    private fun loadQuestion(index: Int) {
        if (index < questions.size) {
            val question = questions[index]
            currentQuestionIndex = index
            _uiState.update {
                it.copy(
                    currentQuestion = question,
                    currentAnswer = arrayOfNulls(question.getAnswer().length),
                    totalQuestions = questions.size,
                    currentQuestionIndex = index + 1,
                    isCompleted = false,
                    showVictory = false,
                    levelPoints = question.points,
                    timeRemaining = 60,
                    isTimerRunning = true
                )
            }
        } else {
            // اكتملت جميع الأسئلة
            _uiState.update { it.copy(isCompleted = true, showVictory = true) }
            updateUserProgress()
        }
    }
    
    private fun startTimer() {
        viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0 && _uiState.value.isTimerRunning) {
                delay(1000)
                _uiState.update { state ->
                    state.copy(timeRemaining = state.timeRemaining - 1)
                }
            }
            
            if (_uiState.value.timeRemaining == 0 && _uiState.value.isTimerRunning) {
                // انتهى الوقت - إجابة خاطئة
                handleWrongAnswer()
            }
        }
    }
    
    fun addChar(char: Char) {
        val currentAnswer = _uiState.value.currentAnswer
        val firstEmptyIndex = currentAnswer.indexOfFirst { it == null }
        
        if (firstEmptyIndex != -1 && _uiState.value.remainingLives > 0 && !_uiState.value.isCompleted) {
            currentAnswer[firstEmptyIndex] = char
            _uiState.update { it.copy(currentAnswer = currentAnswer) }
            
            // تحقق من الإجابة إذا اكتملت
            if (currentAnswer.none { it == null }) {
                checkAnswer()
            }
        }
    }
    
    fun deleteChar() {
        val currentAnswer = _uiState.value.currentAnswer
        val lastFilledIndex = currentAnswer.indexOfLast { it != null }
        
        if (lastFilledIndex != -1) {
            currentAnswer[lastFilledIndex] = null
            _uiState.update { it.copy(currentAnswer = currentAnswer) }
        }
    }
    
    private fun checkAnswer() {
        val currentAnswer = _uiState.value.currentAnswer
        val question = _uiState.value.currentQuestion
        
        if (question != null) {
            val answerString = currentAnswer.joinToString("")
            val correctAnswer = question.getAnswer()
            
            if (answerString.equals(correctAnswer, ignoreCase = true)) {
                // إجابة صحيحة
                handleCorrectAnswer()
            } else {
                // إجابة خاطئة
                handleWrongAnswer()
            }
        }
    }
    
    private fun handleCorrectAnswer() {
        viewModelScope.launch {
            val pointsEarned = _uiState.value.levelPoints
            
            // تحديث النقاط
            questionRepository.addPoints(pointsEarned)
            questionRepository.incrementCorrectAnswers()
            
            _uiState.update { state ->
                state.copy(
                    userPoints = state.userPoints + pointsEarned,
                    correctAnswersCount = state.correctAnswersCount + 1,
                    isTimerRunning = false
                )
            }
            
            delay(1000)
            loadNextQuestion()
        }
    }
    
    private fun handleWrongAnswer() {
        viewModelScope.launch {
            val newLives = _uiState.value.remainingLives - 1
            
            _uiState.update { state ->
                state.copy(
                    remainingLives = newLives,
                    wrongAnswersCount = state.wrongAnswersCount + 1,
                    isTimerRunning = false
                )
            }
            
            if (newLives <= 0) {
                // انتهت المحاولات
                delay(1500)
                _uiState.update { it.copy(isCompleted = true) }
            } else {
                delay(1000)
                // إعادة تعيين الإجابة
                val question = _uiState.value.currentQuestion
                question?.let {
                    _uiState.update { state ->
                        state.copy(
                            currentAnswer = arrayOfNulls(it.getAnswer().length),
                            timeRemaining = 60,
                            isTimerRunning = true
                        )
                    }
                    startTimer()
                }
            }
        }
    }
    
    fun loadNextQuestion() {
        if (currentQuestionIndex + 1 < questions.size) {
            loadQuestion(currentQuestionIndex + 1)
        } else {
            _uiState.update { it.copy(isCompleted = true, showVictory = true) }
            updateUserProgress()
        }
    }
    
    private fun updateUserProgress() {
        viewModelScope.launch {
            questionRepository.incrementGamesPlayed()
            if (difficulty >= _uiState.value.highestLevelUnlocked) {
                questionRepository.updateHighestLevel(difficulty + 1)
            }
        }
    }
    
    fun showHintAd() {
        _uiState.update { it.copy(showHintAd = true) }
    }
    
    fun onHintAdWatched() {
        val question = _uiState.value.currentQuestion
        val currentAnswer = _uiState.value.currentAnswer
        
        if (question != null) {
            val correctAnswer = question.getAnswer()
            
            // ابحث عن أول حرف خاطئ أو فارغ
            for (i in correctAnswer.indices) {
                if (currentAnswer[i] == null || currentAnswer[i] != correctAnswer[i]) {
                    currentAnswer[i] = correctAnswer[i]
                    break
                }
            }
            _uiState.update { it.copy(currentAnswer = currentAnswer) }
        }
        
        _uiState.update { it.copy(showHintAd = false) }
    }
    
    fun onRewardEarned() {
        _uiState.update { it.copy(showRewardedAd = false) }
    }
    
    fun getPendingUrl(): String = _uiState.value.pendingUrl
}

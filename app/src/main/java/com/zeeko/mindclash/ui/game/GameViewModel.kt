package com.zeeko.mindclash.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeeko.mindclash.data.local.QuestionEntity
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

// --- 🧠 حالة اللعبة الشاملة (State) ---
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
    val showCorrectAnimation: Boolean = false,
    val showWrongAnimation: Boolean = false,
    val isHintVisible: Boolean = false
) {
    // الحصول على السؤال الحالي بأمان تام
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

    private var timerJob: Job? = null

    // ==========================================
    // 1. تهيئة المستوى واسترجاع الذاكرة
    // ==========================================
    fun loadLevel(level: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, currentLevel = level) }
            
            // جلب أسئلة هذا المستوى من قاعدة البيانات (Room)
            val levelQuestions = repository.getQuestionsForLevel(level)
            
            // 🚀 البحث في الذاكرة عن حفظ سابق لهذا المستوى
            val savedData = progressRepository.getSavedGameState(level)

            if (savedData != null) {
                // استرجاع اللعبة بالضبط من حيث توقف اللاعب
                _state.update {
                    it.copy(
                        isLoading = false,
                        questions = levelQuestions,
                        currentQuestionIndex = savedData.questionIndex,
                        score = savedData.score,
                        lives = savedData.lives,
                        timeLeft = savedData.timeLeft,
                        userAnswer = savedData.currentAnswer,
                        isHintVisible = savedData.isHintVisible,
                        isGameOver = savedData.lives <= 0,
                        isLevelComplete = false
                    )
                }
            } else {
                // بدء المستوى من الصفر
                _state.update {
                    it.copy(
                        isLoading = false,
                        questions = levelQuestions,
                        currentQuestionIndex = 0,
                        score = 0,
                        lives = 3,
                        timeLeft = 60,
                        userAnswer = "",
                        isHintVisible = false,
                        isGameOver = false,
                        isLevelComplete = false
                    )
                }
            }
            startTimer()
        }
    }

    // ==========================================
    // 2. نظام الحفظ الفوري (Anti-Rage Quit)
    // ==========================================
    private fun persistCurrentState() {
        val currentState = _state.value
        progressRepository.saveGameState(
            level = currentState.currentLevel,
            questionIndex = currentState.currentQuestionIndex,
            score = currentState.score,
            lives = currentState.lives,
            timeLeft = currentState.timeLeft,
            currentAnswer = currentState.userAnswer,
            isHintVisible = currentState.isHintVisible
        )
    }

    // ==========================================
    // 3. إدارة التفاعلات (الكيبورد والتلميحات)
    // ==========================================
    fun onLetterClick(char: Char) {
        val currentState = _state.value
        val answer = currentState.currentQuestion?.answer ?: return

        if (currentState.userAnswer.length < answer.length) {
            val newAnswer = currentState.userAnswer + char
            _state.update { it.copy(userAnswer = newAnswer) }
            persistCurrentState() // حفظ فوري

            if (newAnswer.length == answer.length) {
                checkAnswer(newAnswer)
            }
        }
    }

    fun onDeleteClick() {
        val currentAnswer = _state.value.userAnswer
        if (currentAnswer.isNotEmpty()) {
            _state.update { it.copy(userAnswer = currentAnswer.dropLast(1)) }
            persistCurrentState() // حفظ فوري
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

    // ==========================================
    // 4. خوارزمية التحقق والانتقال
    // ==========================================
    private fun checkAnswer(userAnswer: String) {
        timerJob?.cancel() // إيقاف العداد
        val currentState = _state.value
        val currentQuestion = currentState.currentQuestion ?: return

        if (userAnswer == currentQuestion.answer) {
            // إجابة صحيحة 🌟
            val earnedPoints = currentQuestion.points
            _state.update { it.copy(score = it.score + earnedPoints, showCorrectAnimation = true) }
            
            viewModelScope.launch {
                delay(800) // انتظار الأنميشن
                _state.update { it.copy(showCorrectAnimation = false) }
                nextQuestion()
            }
        } else {
            // إجابة خاطئة ❌
            val newLives = currentState.lives - 1
            _state.update { it.copy(lives = newLives, showWrongAnimation = true, isGameOver = newLives <= 0) }
            
            if (newLives <= 0) {
                progressRepository.clearGameState() // مسح الحفظ لأن اللعبة انتهت
            } else {
                persistCurrentState() // حفظ فقدان الحياة
            }

            viewModelScope.launch {
                delay(500)
                _state.update { it.copy(showWrongAnimation = false, userAnswer = "") }
                if (newLives > 0) startTimer()
            }
        }
    }

    private fun nextQuestion() {
        val currentState = _state.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            // الانتقال للسؤال التالي في نفس المستوى
            _state.update { 
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    userAnswer = "",
                    isHintVisible = false // إخفاء التلميح للسؤال الجديد
                ) 
            }
            startTimer()
        } else {
            // 🏆 إنهاء المستوى بنجاح
            progressRepository.unlockNextLevel(currentState.currentLevel)
            progressRepository.clearGameState() // مسح الحفظ المؤقت لأن المستوى انتهى
            _state.update { it.copy(isLevelComplete = true) }
        }
    }

    // ==========================================
    // 5. نظام العداد الزمني (Timer)
    // ==========================================
    private fun startTimer() {
        timerJob?.cancel()
        // العداد يكمل من الوقت المحفوظ (timeLeft) ولا يبدأ من 60 إذا كان مسترجعاً
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentLeft = _state.value.timeLeft
                if (currentLeft > 0) {
                    _state.update { it.copy(timeLeft = currentLeft - 1) }
                    // نقوم بالحفظ كل 5 ثوانٍ لتخفيف الضغط على المعالج، أو عند تغيير جوهري
                    if (currentLeft % 5 == 0) persistCurrentState()
                } else {
                    handleTimeout()
                    break
                }
            }
        }
    }

    private fun handleTimeout() {
        val newLives = _state.value.lives - 1
        _state.update { it.copy(lives = newLives, isGameOver = newLives <= 0, userAnswer = "") }
        
        if (newLives <= 0) {
            progressRepository.clearGameState()
        } else {
            persistCurrentState()
            startTimer() // إعادة العداد للمحاولة الجديدة
        }
    }

    // إعادة اللعب بعد الخسارة
    fun resetGame() {
        progressRepository.clearGameState()
        loadLevel(_state.value.currentLevel)
    }
}

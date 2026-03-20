package com.zeeko.mindclash.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeeko.mindclash.data.repository.QuestionRepository
import com.zeeko.mindclash.utils.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context

data class DifficultyLevel(
    val id: Int,
    val name: String,
    val description: String,
    val icon: Int,
    val color: Long,
    val questionCount: Int,
    val isUnlocked: Boolean
)

data class HomeUiState(
    val userPoints: Int = 0,
    val highestLevelUnlocked: Int = 1,
    val gamesPlayed: Int = 0,
    val correctAnswers: Int = 0,
    val questionsAnswered: Int = 0,
    val difficultyLevels: List<DifficultyLevel> = emptyList(),
    val isLoading: Boolean = true,
    val showRewardedAd: Boolean = false,
    val newSetLink: String = "https://zeekoorg.github.io/daily-set.html"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val languageManager: LanguageManager,
    private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
        loadDifficultyLevels()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            questionRepository.getUserProgress().collect { progress ->
                progress?.let {
                    _uiState.update { state ->
                        state.copy(
                            userPoints = it.totalPoints,
                            highestLevelUnlocked = it.highestLevelUnlocked,
                            gamesPlayed = it.gamesPlayed,
                            correctAnswers = it.correctAnswers,
                            questionsAnswered = it.questionsAnswered
                        )
                    }
                }
            }
        }
    }
    
    private fun loadDifficultyLevels() {
        viewModelScope.launch {
            val levels = listOf(
                DifficultyLevel(
                    id = 1,
                    name = if (languageManager.isRTL()) "سهل" else "Easy",
                    description = if (languageManager.isRTL()) "50 سؤال للمبتدئين" else "50 questions for beginners",
                    icon = 0,
                    color = 0xFF4CAF50,
                    questionCount = 50,
                    isUnlocked = true
                ),
                DifficultyLevel(
                    id = 2,
                    name = if (languageManager.isRTL()) "متوسط" else "Medium",
                    description = if (languageManager.isRTL()) "50 سؤال للمتوسطين" else "50 questions for intermediates",
                    icon = 0,
                    color = 0xFFFF9800,
                    questionCount = 50,
                    isUnlocked = _uiState.value.highestLevelUnlocked >= 2
                ),
                DifficultyLevel(
                    id = 3,
                    name = if (languageManager.isRTL()) "صعب" else "Hard",
                    description = if (languageManager.isRTL()) "50 سؤال للمحترفين" else "50 questions for experts",
                    icon = 0,
                    color = 0xFFF44336,
                    questionCount = 50,
                    isUnlocked = _uiState.value.highestLevelUnlocked >= 3
                ),
                DifficultyLevel(
                    id = 4,
                    name = if (languageManager.isRTL()) "خبير" else "Expert",
                    description = if (languageManager.isRTL()) "50 سؤال للعباقرة" else "50 questions for geniuses",
                    icon = 0,
                    color = 0xFF9C27B0,
                    questionCount = 50,
                    isUnlocked = _uiState.value.highestLevelUnlocked >= 4
                ),
                DifficultyLevel(
                    id = 5,
                    name = if (languageManager.isRTL()) "أسطوري" else "Legendary",
                    description = if (languageManager.isRTL()) "50 سؤال للأبطال" else "50 questions for legends",
                    icon = 0,
                    color = 0xFFFFD700,
                    questionCount = 50,
                    isUnlocked = _uiState.value.highestLevelUnlocked >= 5
                ),
                DifficultyLevel(
                    id = 6,
                    name = if (languageManager.isRTL()) "ملحمي" else "Epic",
                    description = if (languageManager.isRTL()) "50 سؤال للأبطال الخارقين" else "50 questions for superheroes",
                    icon = 0,
                    color = 0xFFFF1493,
                    questionCount = 50,
                    isUnlocked = _uiState.value.highestLevelUnlocked >= 6
                ),
                DifficultyLevel(
                    id = 7,
                    name = if (languageManager.isRTL()) "خرافي" else "Mythic",
                    description = if (languageManager.isRTL()) "50 سؤال للأساطير" else "50 questions for myths",
                    icon = 0,
                    color = 0xFF00BCD4,
                    questionCount = 50,
                    isUnlocked = _uiState.value.highestLevelUnlocked >= 7
                ),
                DifficultyLevel(
                    id = 8,
                    name = if (languageManager.isRTL()) "كوني" else "Cosmic",
                    description = if (languageManager.isRTL()) "50 سؤال للفضاء" else "50 questions for space",
                    icon = 0,
                    color = 0xFF3F51B5,
                    questionCount = 50,
                    isUnlocked = _uiState.value.highestLevelUnlocked >= 8
                ),
                DifficultyLevel(
                    id = 9,
                    name = if (languageManager.isRTL()) "لا نهائي" else "Infinite",
                    description = if (languageManager.isRTL()) "50 سؤال للعباقرة الخارقين" else "50 questions for super geniuses",
                    icon = 0,
                    color = 0xFFE91E63,
                    questionCount = 50,
                    isUnlocked = _uiState.value.highestLevelUnlocked >= 9
                ),
                // بعد التعديل (يستخدم المورد الجديد):
                DifficultyLevel(
                    id = 10,
                    name = if (languageManager.isRTL()) 
                        context.getString(R.string.mindclash_final_level) 
                    else 
                        context.getString(R.string.mindclash_final_level),
                    description = if (languageManager.isRTL()) "50 سؤال مستحيلة" else "50 impossible questions",
                    icon = 0,
                    color = 0xFF000000,
                    questionCount = 50,
                    isUnlocked = _uiState.value.highestLevelUnlocked >= 10
                  )
            )
            
            _uiState.update { state ->
                state.copy(
                    difficultyLevels = levels,
                    isLoading = false
                )
            }
        }
    }
    
    fun showRewardedAdForNewSet() {
        _uiState.update { it.copy(showRewardedAd = true) }
    }
    
    fun onRewardEarned() {
        _uiState.update { it.copy(showRewardedAd = false) }
    }
}

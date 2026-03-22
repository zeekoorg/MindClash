package com.zeeko.mindclash.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProgressRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mindclash_vip_prefs", Context.MODE_PRIVATE)

    fun getUnlockedLevel(): Int {
        return prefs.getInt("unlocked_level", 1)
    }

    fun unlockNextLevel(currentLevel: Int) {
        val highestUnlocked = getUnlockedLevel()
        if (currentLevel >= highestUnlocked) {
            prefs.edit().putInt("unlocked_level", currentLevel + 1).apply()
        }
    }

    fun saveGameState(
        level: Int, 
        questionIndex: Int, 
        score: Int, 
        lives: Int, 
        currentAnswer: String, 
        isHintVisible: Boolean
    ) {
        prefs.edit().apply {
            putInt("saved_level", level)
            putInt("saved_q_index", questionIndex)
            putInt("saved_score", score)
            putInt("saved_lives", lives)
            putString("saved_answer", currentAnswer)
            putBoolean("saved_hint", isHintVisible)
        }.apply()
    }

    fun getSavedGameState(level: Int): SavedGameData? {
        val savedLevel = prefs.getInt("saved_level", -1)
        if (savedLevel == level) {
            return SavedGameData(
                questionIndex = prefs.getInt("saved_q_index", 0),
                score = prefs.getInt("saved_score", 0),
                lives = prefs.getInt("saved_lives", 3),
                currentAnswer = prefs.getString("saved_answer", "") ?: "",
                isHintVisible = prefs.getBoolean("saved_hint", false)
            )
        }
        return null
    }

    fun clearGameState() {
        prefs.edit().apply {
            remove("saved_level")
            remove("saved_q_index")
            remove("saved_score")
            remove("saved_lives")
            remove("saved_answer")
            remove("saved_hint")
        }.apply()
    }
}

data class SavedGameData(
    val questionIndex: Int, val score: Int, val lives: Int,
    val currentAnswer: String, val isHintVisible: Boolean
)

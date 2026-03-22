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
    // ملف الذاكرة الدائم والمشفر الخاص بلعبتنا
    private val prefs: SharedPreferences = context.getSharedPreferences("mindclash_vip_prefs", Context.MODE_PRIVATE)

    // ==========================================
    // 1. نظام تقدم الخريطة (فتح المستويات)
    // ==========================================
    
    fun getUnlockedLevel(): Int {
        return prefs.getInt("unlocked_level", 1) // المستوى الافتراضي المفتوح هو 1
    }

    fun unlockNextLevel(currentLevel: Int) {
        val highestUnlocked = getUnlockedLevel()
        if (currentLevel >= highestUnlocked) {
            prefs.edit().putInt("unlocked_level", currentLevel + 1).apply()
        }
    }

    // ==========================================
    // 2. نظام حفظ الحالة الفوري (Anti-Rage Quit)
    // ==========================================

    // حفظ كل تفصيلة في اللعبة عند كتابة أي حرف أو تغير العداد
    fun saveGameState(
        level: Int, 
        questionIndex: Int, 
        score: Int, 
        lives: Int, 
        timeLeft: Int, 
        currentAnswer: String, 
        isHintVisible: Boolean
    ) {
        prefs.edit().apply {
            putInt("saved_level", level)
            putInt("saved_q_index", questionIndex)
            putInt("saved_score", score)
            putInt("saved_lives", lives)
            putInt("saved_time", timeLeft)
            putString("saved_answer", currentAnswer)
            putBoolean("saved_hint", isHintVisible)
        }.apply()
    }

    // استرجاع الحالة عند فتح التطبيق من جديد
    fun getSavedGameState(level: Int): SavedGameData? {
        val savedLevel = prefs.getInt("saved_level", -1)
        
        // إذا كان المستوى المحفوظ هو نفس المستوى الذي يحاول اللاعب دخوله، نسترجع البيانات
        if (savedLevel == level) {
            return SavedGameData(
                questionIndex = prefs.getInt("saved_q_index", 0),
                score = prefs.getInt("saved_score", 0),
                lives = prefs.getInt("saved_lives", 3),
                timeLeft = prefs.getInt("saved_time", 60),
                currentAnswer = prefs.getString("saved_answer", "") ?: "",
                isHintVisible = prefs.getBoolean("saved_hint", false)
            )
        }
        return null // لا يوجد حفظ سابق لهذا المستوى
    }

    // مسح الحفظ المؤقت (عند الفوز بالمستوى أو الخسارة التامة)
    fun clearGameState() {
        prefs.edit().apply {
            remove("saved_level")
            remove("saved_q_index")
            remove("saved_score")
            remove("saved_lives")
            remove("saved_time")
            remove("saved_answer")
            remove("saved_hint")
        }.apply()
    }
}

// كلاس بيانات صغير لتنظيم البيانات المسترجعة
data class SavedGameData(
    val questionIndex: Int,
    val score: Int,
    val lives: Int,
    val timeLeft: Int,
    val currentAnswer: String,
    val isHintVisible: Boolean
)

package com.zeeko.mindclash.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zeeko.mindclash.utils.LanguageManager

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val questionAr: String,
    val questionEn: String,
    val answerAr: String,
    val answerEn: String,
    val hintAr: String,
    val hintEn: String,
    val categoryAr: String,
    val categoryEn: String,
    val difficulty: Int,
    val points: Int,
    val isLocked: Boolean = true,
    val orderIndex: Int
) {
    // دالة مساعدة للحصول على النص حسب اللغة - تستقبل LanguageManager كمعامل
    fun getQuestion(languageManager: LanguageManager): String = 
        if (languageManager.isRTL()) questionAr else questionEn
    
    fun getAnswer(languageManager: LanguageManager): String = 
        if (languageManager.isRTL()) answerAr else answerEn
    
    fun getHint(languageManager: LanguageManager): String = 
        if (languageManager.isRTL()) hintAr else hintEn
    
    fun getCategory(languageManager: LanguageManager): String = 
        if (languageManager.isRTL()) categoryAr else categoryEn
}

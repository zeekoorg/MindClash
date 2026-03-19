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
    // دالة مساعدة للحصول على النص حسب اللغة
    fun getQuestion(): String = 
        if (LanguageManager.isRTL()) questionAr else questionEn
    
    fun getAnswer(): String = 
        if (LanguageManager.isRTL()) answerAr else answerEn
    
    fun getHint(): String = 
        if (LanguageManager.isRTL()) hintAr else hintEn
    
    fun getCategory(): String = 
        if (LanguageManager.isRTL()) categoryAr else categoryEn
}

package com.zeeko.mindclash.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    fun getQuestion(isRTL: Boolean): String = if (isRTL) questionAr else questionEn
    fun getAnswer(isRTL: Boolean): String = if (isRTL) answerAr else answerEn
    fun getHint(isRTL: Boolean): String = if (isRTL) hintAr else hintEn
    fun getCategory(isRTL: Boolean): String = if (isRTL) categoryAr else categoryEn
}

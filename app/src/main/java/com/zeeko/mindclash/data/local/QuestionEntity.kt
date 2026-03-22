package com.zeeko.mindclash.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions_table")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val question: String,
    val answer: String,
    val hint: String,
    val category: String,
    val difficulty: Int,
    val points: Int
)

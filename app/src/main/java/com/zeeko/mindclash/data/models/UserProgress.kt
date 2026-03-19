package com.zeeko.mindclash.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey
    val id: Int = 1,
    val totalPoints: Int = 0,
    val highestLevelUnlocked: Int = 1,
    val gamesPlayed: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0
)

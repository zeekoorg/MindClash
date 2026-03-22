package com.zeeko.mindclash.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    // جلب أسئلة عشوائية لمستوى معين
    @Query("SELECT * FROM questions_table WHERE difficulty = :level ORDER BY RANDOM() LIMIT 10")
    suspend fun getQuestionsForLevel(level: Int): List<QuestionEntity>

    // التأكد من أن القاعدة ليست فارغة
    @Query("SELECT COUNT(*) FROM questions_table")
    suspend fun getQuestionsCount(): Int
}

package com.zeeko.mindclash.data.database

import androidx.room.*
import com.zeeko.mindclash.data.models.Question
import com.zeeko.mindclash.data.models.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    
    // عمليات الأسئلة
    @Query("SELECT * FROM questions WHERE difficulty <= :maxDifficulty ORDER BY difficulty ASC, orderIndex ASC")
    fun getQuestionsByDifficulty(maxDifficulty: Int): Flow<List<Question>>
    
    @Query("SELECT * FROM questions WHERE difficulty = :difficulty ORDER BY orderIndex ASC")
    suspend fun getQuestionsByDifficultyLevel(difficulty: Int): List<Question>
    
    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Int): Question
    
    @Query("SELECT * FROM questions WHERE isLocked = 0 ORDER BY difficulty ASC, orderIndex ASC")
    fun getUnlockedQuestions(): Flow<List<Question>>
    
    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionsCount(): Int
    
    @Update
    suspend fun updateQuestion(question: Question)
    
    @Insert
    suspend fun insertAllQuestions(questions: List<Question>)
    
    @Query("UPDATE questions SET isLocked = :isLocked WHERE id = :questionId")
    suspend fun updateQuestionLockStatus(questionId: Int, isLocked: Boolean)
    
    // عمليات تقدم المستخدم
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgress(): Flow<UserProgress?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgress)
    
    @Update
    suspend fun updateUserProgress(progress: UserProgress)
    
    @Query("UPDATE user_progress SET totalPoints = totalPoints + :points WHERE id = 1")
    suspend fun addPoints(points: Int)
    
    @Query("UPDATE user_progress SET highestLevelUnlocked = :level WHERE id = 1 AND highestLevelUnlocked < :level")
    suspend fun updateHighestLevel(level: Int)
    
    @Query("UPDATE user_progress SET gamesPlayed = gamesPlayed + 1 WHERE id = 1")
    suspend fun incrementGamesPlayed()
    
    @Query("UPDATE user_progress SET questionsAnswered = questionsAnswered + 1, correctAnswers = correctAnswers + 1 WHERE id = 1")
    suspend fun incrementCorrectAnswers()
}

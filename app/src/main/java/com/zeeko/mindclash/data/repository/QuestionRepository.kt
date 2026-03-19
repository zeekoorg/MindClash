package com.zeeko.mindclash.data.repository

import com.zeeko.mindclash.data.database.QuestionDao
import com.zeeko.mindclash.data.models.Question
import com.zeeko.mindclash.data.models.UserProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
    private val questionDao: QuestionDao
) {
    
    fun getQuestionsByDifficulty(maxDifficulty: Int): Flow<List<Question>> =
        questionDao.getQuestionsByDifficulty(maxDifficulty)
    
    suspend fun getQuestionsByDifficultyLevel(difficulty: Int): List<Question> =
        questionDao.getQuestionsByDifficultyLevel(difficulty)
    
    suspend fun getQuestionById(id: Int): Question =
        questionDao.getQuestionById(id)
    
    fun getUnlockedQuestions(): Flow<List<Question>> =
        questionDao.getUnlockedQuestions()
    
    suspend fun updateQuestionLockStatus(questionId: Int, isLocked: Boolean) =
        questionDao.updateQuestionLockStatus(questionId, isLocked)
    
    fun getUserProgress(): Flow<UserProgress?> =
        questionDao.getUserProgress()
    
    suspend fun addPoints(points: Int) =
        questionDao.addPoints(points)
    
    suspend fun updateHighestLevel(level: Int) =
        questionDao.updateHighestLevel(level)
    
    suspend fun incrementGamesPlayed() =
        questionDao.incrementGamesPlayed()
    
    suspend fun incrementCorrectAnswers() =
        questionDao.incrementCorrectAnswers()
    
    suspend fun insertUserProgress(progress: UserProgress) =
        questionDao.insertUserProgress(progress)
}

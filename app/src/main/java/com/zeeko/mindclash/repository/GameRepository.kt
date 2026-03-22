package com.zeeko.mindclash.repository

import com.zeeko.mindclash.data.local.QuestionDao
import com.zeeko.mindclash.data.local.QuestionEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val dao: QuestionDao
) {
    // جلب أسئلة المستوى العشوائية من قاعدة البيانات
    suspend fun getQuestionsForLevel(level: Int): List<QuestionEntity> {
        return dao.getQuestionsForLevel(level)
    }

    // التأكد من عدد الأسئلة الكلي
    suspend fun getTotalQuestionsCount(): Int {
        return dao.getQuestionsCount()
    }
}

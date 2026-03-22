package com.zeeko.mindclash.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProgressRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("mindclash_progress", Context.MODE_PRIVATE)

    // جلب أعلى مستوى وصل له اللاعب (الافتراضي 1)
    fun getUnlockedLevel(): Int {
        return prefs.getInt("unlocked_level", 1)
    }

    // فتح المستوى التالي إذا فاز بالمستوى الحالي
    fun unlockNextLevel(currentLevel: Int) {
        val highestUnlocked = getUnlockedLevel()
        if (currentLevel >= highestUnlocked) {
            prefs.edit().putInt("unlocked_level", currentLevel + 1).apply()
        }
    }
}

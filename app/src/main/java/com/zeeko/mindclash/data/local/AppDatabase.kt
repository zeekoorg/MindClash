package com.zeeko.mindclash.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

@Database(entities = [QuestionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mindclash_database"
                )
                .addCallback(AppDatabaseCallback(context, scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(context, database.questionDao())
                }
            }
        }

        private suspend fun populateDatabase(context: Context, dao: QuestionDao) {
            try {
                // 🚀 يجب أن يكون ملف questions.json في مجلد assets
                val inputStream = context.assets.open("questions.json")
                val reader = InputStreamReader(inputStream, "UTF-8")
                val type = object : TypeToken<List<QuestionEntity>>() {}.type
                val questions: List<QuestionEntity> = Gson().fromJson(reader, type)
                
                dao.insertAll(questions)
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

package com.zeeko.mindclash.di

import android.content.Context
import androidx.room.Room
import com.zeeko.mindclash.AdManager
import com.zeeko.mindclash.data.database.AppDatabase
import com.zeeko.mindclash.data.database.QuestionDao
import com.zeeko.mindclash.data.repository.QuestionRepository
import com.zeeko.mindclash.utils.LanguageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mindclash_database"
        ).build()
    }
    
    @Provides
    fun provideQuestionDao(database: AppDatabase): QuestionDao {
        return database.questionDao()
    }
    
    @Provides
    @Singleton
    fun provideQuestionRepository(questionDao: QuestionDao): QuestionRepository {
        return QuestionRepository(questionDao)
    }
    
@Provides
@Singleton
fun provideAdManager(
    @ApplicationContext context: Context,
    languageManager: LanguageManager  // أضف هذا
): AdManager {
    return AdManager(context, languageManager)  // وقم بتحديث هذا
}
    
    @Provides
    @Singleton
    fun provideLanguageManager(@ApplicationContext context: Context): LanguageManager {
        return LanguageManager(context)
    }
}

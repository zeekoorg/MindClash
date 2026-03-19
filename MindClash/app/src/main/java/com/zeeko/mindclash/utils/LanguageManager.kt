package com.zeeko.mindclash.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("language_prefs")

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        const val LANG_ARABIC = "ar"
        const val LANG_ENGLISH = "en"
        private var currentLanguage = LANG_ARABIC
    }
    
    fun init(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.data.collect { preferences ->
                currentLanguage = preferences[LANGUAGE_KEY] ?: LANG_ARABIC
                setLocale(context, currentLanguage)
            }
        }
    }
    
    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
        currentLanguage = languageCode
        setLocale(context, languageCode)
    }
    
    private fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
    
    fun getCurrentLanguage(): String = currentLanguage
    
    fun isRTL(): Boolean = currentLanguage == LANG_ARABIC
    
    fun getString(stringId: Int): String = context.getString(stringId)
    
    companion object {
        fun isRTL(): Boolean = currentLanguage == LANG_ARABIC
    }
}

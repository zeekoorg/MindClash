package com.zeeko.mindclash

import android.app.Application
import android.util.Log
import com.yandex.mobile.ads.common.MobileAds
import com.zeeko.mindclash.utils.LanguageManager
import com.zeeko.mindclash.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MindClashApplication : Application() {
    
    @Inject
    lateinit var languageManager: LanguageManager
    
    override fun onCreate() {
        super.onCreate()
        
        // 1. تهيئة مدير اللغة أولاً لضمان تجهيز النصوص فور تشغيل اللعبة
        languageManager.init(this)
        
        // 2. تعيين المثيل للاستخدام في جميع أنحاء التطبيق
        LanguageManager.setInstance(languageManager)
        
        // 3. تفعيل سجل الأخطاء لياندكس في وضع التطوير فقط (مفيد جداً لكبير المطورين)
        if (BuildConfig.DEBUG) {
            MobileAds.enableLogging(true)
        }
        
        // 4. تهيئة إعلانات ياندكس وشبكات الوساطة (Unity & ironSource) دفعة واحدة
        MobileAds.initialize(this) { 
            Log.d("MindClashAds", "تم تهيئة شبكة الإعلانات والوساطة بنجاح! 🚀")
        }
    }
}

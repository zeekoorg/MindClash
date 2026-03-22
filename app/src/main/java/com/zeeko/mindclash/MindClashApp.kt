package com.zeeko.mindclash

import android.app.Application
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MindClashApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // تهيئة إعلانات ياندكس فور تشغيل التطبيق لضمان سرعة الظهور
        MobileAds.initialize(this) { }
    }
}

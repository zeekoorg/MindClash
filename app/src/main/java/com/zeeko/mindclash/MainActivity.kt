package com.zeeko.mindclash

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.screens.IntroVideoScreen
import com.zeeko.mindclash.ui.screens.MindClashNavGraph
import com.zeeko.mindclash.ui.theme.MindClashTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. قراءة الذاكرة فور فتح التطبيق لمعرفة ما إذا كان المستخدم قد شاهد المقدمة
        val sharedPreferences = getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE)
        val hasSeenIntro = sharedPreferences.getBoolean("HasSeenIntro", false)

        setContent {
            MindClashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. متغير ذكي يتحكم في الشاشة المعروضة بناءً على حالة الذاكرة
                    var showIntro by remember { mutableStateOf(!hasSeenIntro) }

                    if (showIntro) {
                        // 3. عرض شاشة الفيديو السينمائي للاعب الجديد
                        IntroVideoScreen(
                            onVideoFinished = {
                                // بمجرد انتهاء الفيديو أو التخطي، نغير الحالة لتظهر اللعبة
                                showIntro = false
                            }
                        )
                    } else {
                        // 4. الدخول المباشر للعبة (الخريطة والتنقل) للاعبين القدامى
                        MindClashNavGraph(adManager = adManager)
                    }
                }
            }
        }
    }
}

package com.zeeko.mindclash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.zeeko.mindclash.ads.AdManager
import com.zeeko.mindclash.ui.screens.MindClashNavGraph
import com.zeeko.mindclash.ui.theme.MindClashTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // حقن مدير الإعلانات
    @Inject lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MindClashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // تشغيل نظام التنقل وتمرير الإعلانات
                    MindClashNavGraph(adManager = adManager)
                }
            }
        }
    }
}

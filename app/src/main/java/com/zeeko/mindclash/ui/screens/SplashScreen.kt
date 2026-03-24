package com.zeeko.mindclash.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.zeeko.mindclash.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    val context = LocalContext.current

    // الانتقال بعد 4 ثوانٍ (4000 ملي ثانية)
    LaunchedEffect(Unit) {
        delay(4000)
        onNavigateToHome()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    // ربط الفيديو من مجلد raw
                    setVideoURI(Uri.parse("android.resource://${ctx.packageName}/${R.raw.splash_video}"))
                    setOnPreparedListener { it.isLooping = false }
                    start()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

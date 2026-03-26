package com.zeeko.mindclash.ui.screens

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.zeeko.mindclash.R
import kotlinx.coroutines.delay

@Composable
fun IntroVideoScreen(onVideoFinished: () -> Unit) {
    val context = LocalContext.current
    
    // متغير للتحكم بظهور زر التخطي
    var showSkipButton by remember { mutableStateOf(false) }

    // إعداد مشغل الفيديو الاحترافي
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.intro_video}")
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true // تشغيل تلقائي
            
            // مراقبة الفيديو حتى ينتهي
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        markIntroAsSeen(context)
                        onVideoFinished()
                    }
                }
            })
        }
    }

    // مؤقت ظهور زر التخطي (بعد 5 ثواني)
    LaunchedEffect(Unit) {
        delay(5000)
        showSkipButton = true
    }

    // تنظيف المشغل من الذاكرة عند الخروج من الشاشة
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        // عرض الفيديو بحجم الشاشة الكاملة
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // إخفاء أزرار التقديم والتأخير الخاصة بالفيديو
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // زر التخطي بأنيميشن سلس
        AnimatedVisibility(
            visible = showSkipButton,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 20.dp)
        ) {
            Button(
                onClick = {
                    exoPlayer.stop()
                    markIntroAsSeen(context)
                    onVideoFinished()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "تخطي ⏩",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// دالة لحفظ معلومة أن اللاعب قد شاهد الفيديو لكي لا يظهر له مرة أخرى أبداً
private fun markIntroAsSeen(context: Context) {
    val sharedPreferences = context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putBoolean("HasSeenIntro", true).apply()
}

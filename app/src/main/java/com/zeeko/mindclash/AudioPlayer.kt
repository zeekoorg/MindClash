package com.zeeko.mindclash // تأكد من مطابقة الباكدج لمشروعك

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import com.zeeko.mindclash.R

object AudioPlayer {
    private var soundPool: SoundPool? = null
    private var bgmPlayer: MediaPlayer? = null

    // معرفات الأصوات
    private var clickId = 0
    private var correctId = 0
    private var wrongId = 0
    private var winId = 0
    private var loseId = 0

    // حالة الإعدادات
    var isMusicEnabled: Boolean = true
        private set
    var isSfxEnabled: Boolean = true
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE)
        isMusicEnabled = prefs.getBoolean("MusicEnabled", true)
        isSfxEnabled = prefs.getBoolean("SfxEnabled", true)

        if (soundPool == null) {
            soundPool = SoundPool.Builder().setMaxStreams(5).build()
            clickId = soundPool?.load(context, R.raw.sfx_click, 1) ?: 0
            correctId = soundPool?.load(context, R.raw.sfx_correct, 1) ?: 0
            wrongId = soundPool?.load(context, R.raw.sfx_wrong, 1) ?: 0
            winId = soundPool?.load(context, R.raw.sfx_win, 1) ?: 0
            loseId = soundPool?.load(context, R.raw.sfx_lose, 1) ?: 0
        }

        if (bgmPlayer == null) {
            bgmPlayer = MediaPlayer.create(context, R.raw.bgm_game)
            bgmPlayer?.isLooping = true
            bgmPlayer?.setVolume(0.3f, 0.3f)
        }
    }

    // دوال التشغيل مع التحقق من إعدادات المستخدم
    fun playClick() { if (isSfxEnabled) soundPool?.play(clickId, 1f, 1f, 0, 0, 1f) }
    fun playCorrect() { if (isSfxEnabled) soundPool?.play(correctId, 1f, 1f, 0, 0, 1f) }
    fun playWrong() { if (isSfxEnabled) soundPool?.play(wrongId, 1f, 1f, 0, 0, 1f) }
    fun playWin() { if (isSfxEnabled) soundPool?.play(winId, 1f, 1f, 0, 0, 1f) }
    fun playLose() { if (isSfxEnabled) soundPool?.play(loseId, 1f, 1f, 0, 0, 1f) }

    fun startBGM() {
        if (isMusicEnabled && bgmPlayer?.isPlaying == false) {
            bgmPlayer?.start()
        }
    }

    fun pauseBGM() {
        if (bgmPlayer?.isPlaying == true) {
            bgmPlayer?.pause()
        }
    }

    // دوال لتغيير الإعدادات من واجهة المستخدم
    fun setMusicEnabled(context: Context, enabled: Boolean) {
        isMusicEnabled = enabled
        context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE)
            .edit().putBoolean("MusicEnabled", enabled).apply()
        
        if (enabled) startBGM() else pauseBGM()
    }

    fun setSfxEnabled(context: Context, enabled: Boolean) {
        isSfxEnabled = enabled
        context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE)
            .edit().putBoolean("SfxEnabled", enabled).apply()
    }
}

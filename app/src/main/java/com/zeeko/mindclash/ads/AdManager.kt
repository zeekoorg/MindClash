package com.zeeko.mindclash.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    
    private var isRewardedLoading = false
    private var isInterstitialLoading = false

    companion object {
        // 🧪 معرفات ياندكس التجريبية (Test IDs)
        const val REWARDED_ID = "demo-rewarded-yandex"
        const val INTERSTITIAL_ID = "demo-interstitial-yandex"
    }

    init {
        // بدء التحميل المسبق فور تشغيل التطبيق
        loadRewardedAd()
        loadInterstitialAd()
    }

    private fun loadRewardedAd() {
        if (isRewardedLoading) return
        isRewardedLoading = true

        val loader = RewardedAdLoader(context)
        loader.setAdLoadListener(object : RewardedAdLoadListener {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isRewardedLoading = false
                Log.d("AdManager", "Rewarded Ad Loaded!")
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                isRewardedLoading = false
                Log.e("AdManager", "Failed to load Rewarded Ad: ${error.description}")
                // إعادة المحاولة بعد 30 ثانية عند الفشل
                Handler(Looper.getMainLooper()).postDelayed({ loadRewardedAd() }, 30000)
            }
        })
        loader.loadAd(AdRequestConfiguration.Builder(REWARDED_ID).build())
    }

    private fun loadInterstitialAd() {
        if (isInterstitialLoading) return
        isInterstitialLoading = true

        val loader = InterstitialAdLoader(context)
        loader.setAdLoadListener(object : InterstitialAdLoadListener {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isInterstitialLoading = false
                Log.d("AdManager", "Interstitial Ad Loaded!")
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                isInterstitialLoading = false
                Log.e("AdManager", "Failed to load Interstitial Ad: ${error.description}")
                Handler(Looper.getMainLooper()).postDelayed({ loadInterstitialAd() }, 30000)
            }
        })
        loader.loadAd(AdRequestConfiguration.Builder(INTERSTITIAL_ID).build())
    }

    // عرض إعلان المكافأة (مثال: للحصول على تلميح أو محاولة)
    fun showRewardedAd(
        activity: Activity, 
        onRewardEarned: () -> Unit, 
        onAdFailed: () -> Unit
    ) {
        rewardedAd?.let { ad ->
            ad.setAdEventListener(object : RewardedAdEventListener {
                override fun onRewarded(reward: Reward) { onRewardEarned() }
                override fun onAdFailedToShow(error: AdError) { onAdFailed() }
                override fun onAdDismissed() { 
                    rewardedAd = null
                    loadRewardedAd() // تحميل إعلان جديد فور إغلاق الحالي
                }
                override fun onAdShown() {}
                override fun onAdClicked() {}
                override fun onAdImpression(data: ImpressionData?) {}
            })
            ad.show(activity)
        } ?: run {
            onAdFailed()
            loadRewardedAd()
        }
    }

    // عرض إعلان بيني (مثال: عند الخسارة أو انتهاء المستوى)
    fun showInterstitialAd(
        activity: Activity, 
        onAdDismissed: () -> Unit
    ) {
        interstitialAd?.let { ad ->
            ad.setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdDismissed() {
                    interstitialAd = null
                    onAdDismissed()
                    loadInterstitialAd() // تحميل إعلان جديد
                }
                override fun onAdFailedToShow(error: AdError) { onAdDismissed() }
                override fun onAdShown() {}
                override fun onAdClicked() {}
                override fun onAdImpression(data: ImpressionData?) {}
            })
            ad.show(activity)
        } ?: run {
            onAdDismissed()
            loadInterstitialAd()
        }
    }
}

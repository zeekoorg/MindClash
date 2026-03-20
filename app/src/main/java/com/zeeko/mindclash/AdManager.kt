package com.zeeko.mindclash

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import com.yandex.mobile.ads.common.*
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    private val context: Context
) {
    
    // ============================================================
    // متغيرات الإعلان المكافئ (Rewarded Ad)
    // ============================================================
    private var mRewardedAd: RewardedAd? = null
    private var isRewardLoaded = false
    private var rewardedAdLoader: RewardedAdLoader? = null
    private var userEarnedReward = false
    private var pendingUrlAfterAd = ""
    private var isShowingAdDialog = false
    private var currentAdDialog: Dialog? = null
    private var adLoadRetryCount = 0
    private val MAX_RETRY_COUNT = 3
    
    // ============================================================
    // متغيرات الإعلان البيني (Interstitial Ad)
    // ============================================================
    private var mInterstitialAd: InterstitialAd? = null
    private var isInterstitialLoaded = false
    private var interstitialAdLoader: InterstitialAdLoader? = null
    
    // معرفات الإعلانات (Yandex)
    companion object {
        const val YANDEX_BANNER_ID = "demo-banner-yandex"
        const val YANDEX_REWARDED_ID = "demo-rewarded-yandex"
        const val YANDEX_INTERSTITIAL_ID = "demo-interstitial-yandex"
    }
    
    init {
        loadRewardedAd()
        loadInterstitialAd()
    }
    
    // ============================================================
    // دوال الإعلان المكافئ (Rewarded Ad)
    // ============================================================
    
    private fun loadRewardedAd() {
        isRewardLoaded = false
        
        if (rewardedAdLoader == null) {
            rewardedAdLoader = RewardedAdLoader(context)
            rewardedAdLoader?.setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    isRewardLoaded = true
                    adLoadRetryCount = 0
                }
                
                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                    isRewardLoaded = false
                    if (adLoadRetryCount < MAX_RETRY_COUNT) {
                        adLoadRetryCount++
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadRewardedAd()
                        }, 2000)
                    }
                }
            })
        }
        
        rewardedAdLoader?.loadAd(AdRequestConfiguration.Builder(YANDEX_REWARDED_ID).build())
    }
    
    fun showRewardedAd(activity: Activity, url: String = "") {
        pendingUrlAfterAd = url
        showAdDialog(activity)
    }
    
    private fun showRewardedAdInternal(activity: Activity) {
        if (mRewardedAd != null && isRewardLoaded) {
            mRewardedAd?.setAdEventListener(object : RewardedAdEventListener {
                override fun onAdShown() {}
                
                override fun onAdFailedToShow(adError: AdError) {
                    Handler(Looper.getMainLooper()).post {
                        showAdLoadErrorDialog(activity)
                    }
                }
                
                override fun onAdDismissed() {
                    if (userEarnedReward) {
                        Handler(Looper.getMainLooper()).post {
                            giveUserReward(activity)
                        }
                        userEarnedReward = false
                    }
                    
                    loadRewardedAd()
                    
                    Handler(Looper.getMainLooper()).post {
                        if (currentAdDialog != null && currentAdDialog?.isShowing == true) {
                            currentAdDialog?.dismiss()
                            currentAdDialog = null
                            isShowingAdDialog = false
                        }
                    }
                }
                
                override fun onAdClicked() {}
                
                override fun onAdImpression(impressionData: ImpressionData?) {}
                
                override fun onRewarded(reward: Reward) {
                    Handler(Looper.getMainLooper()).post {
                        userEarnedReward = true
                    }
                }
            })
            
            mRewardedAd?.show(activity)
            
        } else {
            Handler(Looper.getMainLooper()).post {
                showAdLoadingDialog(activity)
            }
        }
    }
    
    private fun giveUserReward(activity: Activity) {
        if (currentAdDialog != null && currentAdDialog?.isShowing == true) {
            currentAdDialog?.dismiss()
            currentAdDialog = null
        }
        
        isShowingAdDialog = false
        
        if (pendingUrlAfterAd.isNotEmpty()) {
            openDownloadUrl(activity, pendingUrlAfterAd)
            pendingUrlAfterAd = ""
        }
        
        loadRewardedAd()
    }
    
    private fun showAdLoadingDialog(activity: Activity) {
        val density = activity.resources.displayMetrics.density
        
        val loadingDialog = Dialog(activity)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val dialogLayout = LinearLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(
                (20 * density).toInt(),
                (20 * density).toInt(),
                (20 * density).toInt(),
                (20 * density).toInt()
            )
            
            val dialogBackground = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 25 * density
                gradientType = GradientDrawable.LINEAR_GRADIENT
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                colors = intArrayOf(
                    Color.parseColor("#03330C"),
                    Color.parseColor("#FF002A3E"),
                    Color.parseColor("#FF001925")
                )
                setStroke((2 * density).toInt(), Color.parseColor("#09DDE0"))
            }
            background = dialogBackground
        }
        
        val messageText = TextView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (20 * density).toInt())
            }
            text = "جاري تـحـميل الإعـــلان..." // نص عربي فقط
            setTextColor(Color.WHITE)
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        
        val progressBar = ProgressBar(activity, null, android.R.attr.progressBarStyleLarge).apply {
            layoutParams = LinearLayout.LayoutParams(
                (60 * density).toInt(),
                (60 * density).toInt()
            ).apply {
                gravity = Gravity.CENTER
                setMargins(0, (10 * density).toInt(), 0, (10 * density).toInt())
            }
            isIndeterminate = true
            indeterminateDrawable?.setColorFilter(
                Color.parseColor("#09DDE0"),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
        
        dialogLayout.addView(messageText)
        dialogLayout.addView(progressBar)
        
        loadingDialog.setContentView(dialogLayout)
        loadingDialog.setCancelable(false)
        
        loadingDialog.window?.setLayout(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        loadingDialog.window?.setGravity(Gravity.CENTER)
        
        loadingDialog.show()
        
        val handler = Handler(Looper.getMainLooper())
        val checkRunnable = object : Runnable {
            override fun run() {
                if (loadingDialog.isShowing) {
                    if (isRewardLoaded) {
                        loadingDialog.dismiss()
                        showRewardedAdInternal(activity)
                    } else {
                        if (adLoadRetryCount >= MAX_RETRY_COUNT) {
                            loadingDialog.dismiss()
                            showAdLoadErrorDialog(activity)
                        } else {
                            loadRewardedAd()
                            handler.postDelayed(this, 2000)
                        }
                    }
                }
            }
        }
        
        handler.postDelayed(checkRunnable, 2000)
    }
    
    private fun showAdLoadErrorDialog(activity: Activity) {
        val density = activity.resources.displayMetrics.density
        
        val errorDialog = Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        
        val dialogLayout = LinearLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                (280 * density).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(
                (30 * density).toInt(),
                (30 * density).toInt(),
                (30 * density).toInt(),
                (30 * density).toInt()
            )
            
            val dialogBackground = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 25 * density
                gradientType = GradientDrawable.LINEAR_GRADIENT
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                colors = intArrayOf(
                    Color.parseColor("#03330C"),
                    Color.parseColor("#FF002A3E"),
                    Color.parseColor("#FF001925")
                )
                setStroke((2 * density).toInt(), Color.parseColor("#09DDE0"))
            }
            background = dialogBackground
        }
        
        val messageText = TextView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (25 * density).toInt())
            }
            text = "فـشــل تـحـمـيـل الإعــلان 😥\nحاول مرة أخرى لاحقاً" // نص عربي فقط
            setTextColor(Color.WHITE)
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setLineSpacing((8 * density).toFloat(), 1f)
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        
        val okButton = Button(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (45 * density).toInt()
            )
            text = "موافق" // نص عربي فقط
            setTextColor(Color.WHITE)
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            
            val buttonBackground = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 22 * density
                color = Color.parseColor("#8002573A")
                setStroke((1.5f * density).toInt(), Color.parseColor("#FF685203"))
                gradientType = GradientDrawable.LINEAR_GRADIENT
                colors = intArrayOf(
                    Color.parseColor("#FF001D1C"),
                    Color.parseColor("#FF005A55")
                )
            }
            background = buttonBackground
        }
        
        dialogLayout.addView(messageText)
        dialogLayout.addView(okButton)
        
        errorDialog.setContentView(dialogLayout)
        errorDialog.setCancelable(true)
        
        okButton.setOnClickListener {
            errorDialog.dismiss()
            isShowingAdDialog = false
            pendingUrlAfterAd = ""
            loadRewardedAd()
        }
        
        errorDialog.show()
    }
    
    private fun showAdDialog(activity: Activity) {
        val density = activity.resources.displayMetrics.density
        isShowingAdDialog = true
        
        val adDialog = Dialog(activity).apply {
            currentAdDialog = this
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        
        val dialogLayout = LinearLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                (280 * density).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(
                (30 * density).toInt(),
                (30 * density).toInt(),
                (30 * density).toInt(),
                (30 * density).toInt()
            )
            
            val dialogBackground = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 25 * density
                gradientType = GradientDrawable.LINEAR_GRADIENT
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                colors = intArrayOf(
                    Color.parseColor("#03330C"),
                    Color.parseColor("#FF002A3E"),
                    Color.parseColor("#FF001925")
                )
                setStroke((2 * density).toInt(), Color.parseColor("#09DDE0"))
            }
            background = dialogBackground
        }
        
        val messageText = TextView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (25 * density).toInt())
            }
            text = "للإستمرار يجـب علـيـك مـشـاهـدة إعــلان كـ دعـمـاً لـي لأسـتـمـر بـتقـديـم الـمـزيـد 🤍" // نص عربي فقط
            setTextColor(Color.WHITE)
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setLineSpacing((8 * density).toFloat(), 1f)
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        
        val buttonsLayout = LinearLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        
        val buttonBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 22 * density
            color = Color.parseColor("#8002573A")
            setStroke((1.5f * density).toInt(), Color.parseColor("#FF685203"))
            gradientType = GradientDrawable.LINEAR_GRADIENT
            colors = intArrayOf(
                Color.parseColor("#FF001D1C"),
                Color.parseColor("#FF005A55")
            )
        }
        
        val watchButton = Button(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (45 * density).toInt()
            ).apply {
                setMargins(0, 0, 0, (10 * density).toInt())
            }
            text = "مـشـاهـدة" // نص عربي فقط
            setTextColor(Color.WHITE)
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            background = buttonBackground
        }
        
        val cancelButton = Button(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (45 * density).toInt()
            )
            text = "إلـغـاء" // نص عربي فقط
            setTextColor(Color.WHITE)
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            background = buttonBackground
        }
        
        dialogLayout.addView(messageText)
        buttonsLayout.addView(watchButton)
        buttonsLayout.addView(cancelButton)
        dialogLayout.addView(buttonsLayout)
        
        adDialog.setContentView(dialogLayout)
        adDialog.setCancelable(false)
        
        cancelButton.setOnClickListener {
            isShowingAdDialog = false
            pendingUrlAfterAd = ""
            currentAdDialog = null
            adDialog.dismiss()
        }
        
        watchButton.setOnClickListener {
            showRewardedAdInternal(activity)
        }
        
        adDialog.show()
    }
    
    private fun openDownloadUrl(activity: Activity, url: String) {
        try {
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(Color.parseColor("#012638"))
            builder.setSecondaryToolbarColor(Color.parseColor("#03330C"))
            builder.setStartAnimations(activity, android.R.anim.fade_in, android.R.anim.fade_out)
            builder.setExitAnimations(activity, android.R.anim.fade_in, android.R.anim.fade_out)
            
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity, android.net.Uri.parse(url))
            
        } catch (e: Exception) {
            try {
                val downloadIntent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(url)
                )
                activity.startActivity(downloadIntent)
            } catch (ex: Exception) {
                android.widget.Toast.makeText(
                    activity, 
                    "خطأ في فتح رابط التحميل", // نص عربي فقط
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    // ============================================================
    // دوال الإعلان البيني (Interstitial Ad)
    // ============================================================
    
    private fun loadInterstitialAd() {
        if (interstitialAdLoader == null) {
            interstitialAdLoader = InterstitialAdLoader(context)
            interstitialAdLoader?.setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    isInterstitialLoaded = true
                }
                
                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                    isInterstitialLoaded = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadInterstitialAd()
                    }, 30000)
                }
            })
        }
        
        interstitialAdLoader?.loadAd(AdRequestConfiguration.Builder(YANDEX_INTERSTITIAL_ID).build())
    }
    
    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
        if (mInterstitialAd != null && isInterstitialLoaded) {
            mInterstitialAd?.show(activity)
            
            mInterstitialAd?.setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdShown() {}
                
                override fun onAdFailedToShow(adError: AdError) {
                    onAdClosed.invoke()
                    loadInterstitialAd()
                }
                
                override fun onAdDismissed() {
                    onAdClosed.invoke()
                    loadInterstitialAd()
                }
                
                override fun onAdClicked() {}
                
                override fun onAdImpression(impressionData: ImpressionData?) {}
            })
        } else {
            onAdClosed.invoke()
            loadInterstitialAd()
        }
    }
    
    // ============================================================
    // دالة البانر (Banner Ad)
    // ============================================================
    
    fun createBannerAdView(activity: Activity): com.yandex.mobile.ads.banner.BannerAdView {
        return com.yandex.mobile.ads.banner.BannerAdView(activity).apply {
            adUnitId = YANDEX_BANNER_ID
            val width = (activity.resources.displayMetrics.widthPixels / activity.resources.displayMetrics.density).toInt()
            setAdSize(com.yandex.mobile.ads.banner.BannerAdSize.stickySize(activity, width))
            loadAd(AdRequest.Builder().build())
        }
    }
}

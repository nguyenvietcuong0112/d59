//package com.removedust.speaker.cleaner.presentation.ui.splash
//
//import android.content.Intent
//import android.os.Handler
//import android.os.Looper
//import android.view.View
//import androidx.lifecycle.lifecycleScope
//import com.google.android.gms.ads.LoadAdError
//import com.mallegan.ads.callback.InterCallback
//import com.mallegan.ads.util.Admob
//import com.mallegan.ads.util.ConsentHelper
//import com.removedust.speaker.cleaner.R
//import com.removedust.speaker.cleaner.base.BaseActivity
//import com.removedust.speaker.cleaner.databinding.ActivitySplashBinding
//import com.removedust.speaker.cleaner.domain.adjust.RetentionTracker
//import com.removedust.speaker.cleaner.domain.remoteconfig.RemoteConfigManager
//import com.removedust.speaker.cleaner.domain.remoteconfig.getRemoteAdId
//import com.removedust.speaker.cleaner.presentation.ui.language.LanguageActivity
//import com.removedust.speaker.cleaner.util.ActivityFullCallback
//import com.removedust.speaker.cleaner.util.ActivityLoadNativeFullV1
//import com.removedust.speaker.cleaner.util.SharePreferenceUtils
//import com.removedust.speaker.cleaner.util.LogEvent
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withTimeoutOrNull
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class SplashActivity1 : BaseActivity() {
//
//    private val remoteConfigManager: RemoteConfigManager by lazy {
//        RemoteConfigManager.getInstance()
//    }
//
//    private lateinit var binding: ActivitySplashBinding
//    private var interCallback: InterCallback? = null
//    private var isTransitioning = false
//
//    override fun bind() {
//        binding = ActivitySplashBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            RetentionTracker.checkAndTrackRetention(this@SplashActivity1)
//        }
//
//        lifecycleScope.launch {
//            try {
//                withTimeoutOrNull(3000) {
//                    remoteConfigManager.fetchAndActivate()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//            runOnUiThread {
//                initAdsAfterConfig()
//            }
//        }
//    }
//
//    private fun initAdsAfterConfig() {
//        binding.frAdsBanner.visibility = View.VISIBLE
//
//        var actualLoadedAdId = remoteConfigManager.getAdId("inter_splash", getString(R.string.inter_splash))
//
//        interCallback = object : InterCallback() {
//            override fun onInterstitialLoad(interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd) {
//                super.onInterstitialLoad(interstitialAd)
//                actualLoadedAdId = interstitialAd.adUnitId
//            }
//
//            override fun onAdClosedByUser() {
//                super.onAdClosedByUser()
//                LogEvent.log(this@SplashActivity1, "inter_splash_view")
//
//
//
//                if (!SharePreferenceUtils.isOrganic(applicationContext)) {
//                    ActivityLoadNativeFullV1.open(
//                        this@SplashActivity1,
//                        getRemoteAdId("native_splash_full_high", R.string.native_splash_full_high),
//                        getRemoteAdId("native_splash_full", R.string.native_splash_full),
//                        object : ActivityFullCallback {
//                            override fun onResultFromActivityFull() {
//                                startLanguage()
//                            }
//                        })
//                } else {
//                    startLanguage()
//                }
//            }
//
//            override fun onAdFailedToLoad(i: LoadAdError?) {
//                super.onAdFailedToLoad(i)
//
//                if (!SharePreferenceUtils.isOrganic(applicationContext)) {
//                    ActivityLoadNativeFullV1.open(
//                        this@SplashActivity1,
//                        getRemoteAdId("native_splash_full_high", R.string.native_splash_full_high),
//                        getRemoteAdId("native_splash_full", R.string.native_splash_full),
//                        object : ActivityFullCallback {
//                            override fun onResultFromActivityFull() {
//                                startLanguage()
//                            }
//                        })
//                } else {
//                    startLanguage()
//                }
//            }
//        }
//        loadAdsInter()
//    }
//
//    private fun loadAdsInter() {
//        lifecycleScope.launch {
//            for (progress in 0..99) {
//                binding.tvProgressPercent.text = "$progress%"
//                delay(150L)
//            }
//        }
//
//        val consentHelper = ConsentHelper.getInstance(this)
//        if (!consentHelper.canLoadAndShowAds()) {
//            consentHelper.reset()
//        }
//
//        consentHelper.obtainConsentAndShow(this) {
//            // Load banner ad after consent is obtained
//            val bannerId = getRemoteAdId("banner_splash", R.string.banner_splash)
//            Admob.getInstance().loadBanner(this@SplashActivity1, bannerId)
//            LogEvent.log(this@SplashActivity1, "banner_splash_view")
//
//            // Load splash interstitial ad after consent is obtained
//            Handler(Looper.getMainLooper()).postDelayed({
//                if (!isFinishing && !isDestroyed) {
//                    val highId = remoteConfigManager.getAdId("inter_splash_high", getString(R.string.inter_splash_high))
//                    val interId = remoteConfigManager.getAdId("inter_splash", getString(R.string.inter_splash))
//
//
//                    Admob.getInstance().loadSplashInterAdsFloor(
//                        this@SplashActivity1,
//                        arrayListOf(highId, interId),
//                        2000,
//                        interCallback
//                    )
//                }
//            }, 500)
//        }
//    }
//
//    private fun startLanguage() {
//        if (isTransitioning) return
//        isTransitioning = true
//
//        val nextIntent = Intent(this, LanguageActivity::class.java)
//        startActivity(nextIntent)
//        finish()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Admob.getInstance().dismissLoadingDialog()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        val deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "unknown_device"
//
//        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallback, 5000)
//    }
//}
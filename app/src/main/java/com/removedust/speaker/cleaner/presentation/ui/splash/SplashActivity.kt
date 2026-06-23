package com.removedust.speaker.cleaner.presentation.ui.splash

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.LoadAdError
import com.mallegan.ads.callback.InterCallback
import com.mallegan.ads.util.Admob
import com.mallegan.ads.util.ConsentHelper
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.base.BaseActivity
import com.removedust.speaker.cleaner.databinding.ActivitySplashBinding
import com.removedust.speaker.cleaner.domain.adjust.RetentionTracker
import com.removedust.speaker.cleaner.domain.remoteconfig.RemoteConfigManager
import com.removedust.speaker.cleaner.domain.remoteconfig.getRemoteAdId
import com.removedust.speaker.cleaner.presentation.ui.language.LanguageActivity
import com.removedust.speaker.cleaner.presentation.ui.main.MainActivity
import com.removedust.speaker.cleaner.util.ActivityFullCallback
import com.removedust.speaker.cleaner.util.ActivityLoadNativeFullV1
import com.removedust.speaker.cleaner.util.SharePreferenceUtils
import com.removedust.speaker.cleaner.util.LogEvent
import com.removedust.speaker.cleaner.util.SystemUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private val remoteConfigManager: RemoteConfigManager by lazy {
        RemoteConfigManager.getInstance()
    }

    private lateinit var binding: ActivitySplashBinding
    private var interCallback: InterCallback? = null
    private var isTransitioning = false

    override fun bind() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch(Dispatchers.IO) {
            RetentionTracker.checkAndTrackRetention(this@SplashActivity)
        }

        lifecycleScope.launch {
            try {
                withTimeoutOrNull(3000) {
                    remoteConfigManager.fetchAndActivate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            runOnUiThread {
                initAdsAfterConfig()
            }
        }
    }

    private fun initAdsAfterConfig() {
        binding.frAdsBanner.visibility = View.VISIBLE
        Admob.getInstance()
            .loadBanner(this, getRemoteAdId("banner_splash", R.string.banner_splash))
        LogEvent.log(this, "banner_splash_view")

        interCallback = object : InterCallback() {

            override fun onAdClosedByUser() {
                super.onAdClosedByUser()
                if (!SharePreferenceUtils.isOrganic(applicationContext)) {
                    ActivityLoadNativeFullV1.open(
                        this@SplashActivity,
                        getRemoteAdId("native_splash_full_high", R.string.native_splash_full_high),
                        getRemoteAdId("native_splash_full", R.string.native_splash_full),
                        object : ActivityFullCallback {
                            override fun onResultFromActivityFull() {
                                startLanguage()
                            }
                        })
                } else {
                    startLanguage()
                }
            }

            override fun onAdFailedToLoad(i: LoadAdError?) {
                super.onAdFailedToLoad(i)
                if (!SharePreferenceUtils.isOrganic(applicationContext)) {
                    ActivityLoadNativeFullV1.open(
                        this@SplashActivity,
                        getRemoteAdId("native_splash_full_high", R.string.native_splash_full_high),
                        getRemoteAdId("native_splash_full", R.string.native_splash_full),
                        object : ActivityFullCallback {
                            override fun onResultFromActivityFull() {
                                startLanguage()
                            }
                        })
                } else {
                    startLanguage()
                }
            }
        }
        loadAdsInter()
    }

    private fun loadAdsInter() {
        lifecycleScope.launch {
            for (progress in 0..100) {
                binding.tvProgressPercent.text = "$progress%"
                delay(80L)
            }
        }

        val consentHelper = ConsentHelper.getInstance(this)
        if (!consentHelper.canLoadAndShowAds()) {
            consentHelper.reset()
        }

        consentHelper.obtainConsentAndShow(this) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isFinishing && !isDestroyed) {
                    Admob.getInstance().loadSplashInterAdsFloor(
                        this@SplashActivity,
                        arrayListOf(
                            remoteConfigManager.getAdId(
                                "inter_splash_high",
                                getString(R.string.inter_splash_high)
                            ),
                            remoteConfigManager.getAdId(
                                "inter_splash",
                                getString(R.string.inter_splash)
                            )
                        ),
                        5000,
                        interCallback
                    )
                }
            }, 2000)
        }
    }

    private fun startLanguage() {
        if (isTransitioning) return
        isTransitioning = true

        val nextIntent = Intent(this, LanguageActivity::class.java)
        startActivity(nextIntent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        Admob.getInstance().dismissLoadingDialog()
    }

    override fun onResume() {
        super.onResume()
        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallback, 3000)
    }
}
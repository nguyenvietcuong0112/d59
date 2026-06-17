package com.removedust.speaker.cleaner.presentation.ui.onboarding.fragment

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

import com.mallegan.ads.callback.NativeCallback
import com.mallegan.ads.util.Admob
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.base.AbsBaseFragment
import com.removedust.speaker.cleaner.databinding.FragmentAdsBinding
import com.removedust.speaker.cleaner.domain.remoteconfig.RemoteConfigManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentIntro34ads : AbsBaseFragment<FragmentAdsBinding?>() {
    var viewPager: ViewPager2? = null
    private var countDownTimer: CountDownTimer? = null
    private var adLoadedState = 0 // 0: LOADING, 1: LOADED, 2: FAILED
    private var timerStarted = false
    private var closeButton: ImageView? = null

    override val layout: Int
        get() = R.layout.fragment_ads

    override fun initView() {
        viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
        val adId = try {
            RemoteConfigManager.getInstance()
                .getAdId("native_onboarding_full_2", getString(R.string.native_onboarding_full_2))
        } catch (e: Exception) {
            getString(R.string.native_onboarding_full_2)
        }
        if (adId.isNullOrEmpty()) {
            adLoadedState = 2
            if (viewPager?.currentItem == 4) {
                navigateNext()
            }
        } else {
            loadNativeFull(adId)
        }
    }

    private fun navigateNext() {
        if (isAdded) {
            viewPager?.currentItem = (viewPager?.currentItem ?: 4) + 1
        }
    }

    private fun startTimer() {
        if (timerStarted || !isAdded) return
        timerStarted = true
        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                if (!isAdded) return
                closeButton?.setOnClickListener(View.OnClickListener { v: View? ->
                    navigateNext()
                })
            }
        }.start()
    }

    private fun loadNativeFull(adId: String?) {
        val activity = requireActivity()
        Admob.getInstance().loadNativeAds(activity, adId, 1, object : NativeCallback() {
            override fun onAdFailedToLoad() {
                super.onAdFailedToLoad()
                adLoadedState = 2
                if (isAdded && viewPager?.currentItem == 4) {
                    navigateNext()
                }
            }

            override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                super.onNativeAdLoaded(nativeAd)
                if (!isAdded) return
                adLoadedState = 1
                val adView = LayoutInflater.from(activity)
                    .inflate(R.layout.native_full_language, null) as NativeAdView
                closeButton = adView.findViewById<ImageView>(R.id.close)
                val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
                closeButton?.setOnClickListener(View.OnClickListener { v: View? -> mediaView.performClick() })
                
                binding!!.frAdsFull.removeAllViews()
                binding!!.frAdsFull.addView(adView)
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)

                if (viewPager?.currentItem == 4) {
                    startTimer()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (adLoadedState == 2) {
            navigateNext()
        } else if (adLoadedState == 1 && !timerStarted) {
            startTimer()
        }
    }

    override fun onDestroyView() {
        countDownTimer?.cancel()
        super.onDestroyView()
    }
}

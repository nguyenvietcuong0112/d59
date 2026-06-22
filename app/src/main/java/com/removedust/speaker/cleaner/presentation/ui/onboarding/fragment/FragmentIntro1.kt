package com.removedust.speaker.cleaner.presentation.ui.onboarding.fragment

import android.view.LayoutInflater
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mallegan.ads.callback.NativeCallback
import com.mallegan.ads.util.Admob
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.base.AbsBaseFragment
import com.removedust.speaker.cleaner.databinding.FragmentIntro1Binding
import com.removedust.speaker.cleaner.domain.remoteconfig.RemoteConfigManager
import com.removedust.speaker.cleaner.util.AdsConfig
import com.removedust.speaker.cleaner.util.LogEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentIntro1 : AbsBaseFragment<FragmentIntro1Binding?>() {
    override val layout: Int
        get() = R.layout.fragment_intro1

    override fun initView() {
        val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
        binding!!.txtNext.setOnClickListener(View.OnClickListener { view: View? ->
            viewPager.currentItem = viewPager.currentItem + 1
        })
        loadAdsIntro1()
    }

    private fun loadAdsIntro1() {
        if (AdsConfig.nativeIntro1 != null) {
            val activity = requireActivity()
            LogEvent.log(activity, "native_onboarding_1_view")
            val adView = LayoutInflater.from(activity)
                .inflate(R.layout.layout_native_media, null) as NativeAdView

            binding!!.flAdPlaceholder.removeAllViews()
            binding!!.flAdPlaceholder.addView(adView)
            Admob.getInstance().pushAdsToViewCustom(AdsConfig.nativeIntro1, adView)

            binding!!.shimmerContainer.visibility = View.GONE
            binding!!.loadingProgress.visibility = View.GONE
            binding!!.txtNext.visibility = View.VISIBLE
        } else {
            loadAdsIntro1Dynamically()
        }
    }

    private fun loadAdsIntro1Dynamically() {
        val activity = requireActivity()
        val adId = try {
            RemoteConfigManager.getInstance()
                .getAdId("native_onboarding_1", getString(R.string.native_onboarding_1))
        } catch (e: Exception) {
            getString(R.string.native_onboarding_1)
        }

        if (adId.isNotEmpty()) {
            binding!!.txtNext.visibility = View.INVISIBLE
            binding!!.loadingProgress.visibility = View.VISIBLE
            binding!!.shimmerContainer.visibility = View.GONE

            Admob.getInstance().loadNativeAd(activity, adId, object : NativeCallback() {
                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    if (!isAdded) return
                    goneAds()
                    binding!!.root.postDelayed({
                        if (isAdded) {
                            binding!!.txtNext.visibility = View.VISIBLE
                        }
                    }, 500)
                }

                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    if (!isAdded) return
                    LogEvent.log(activity, "native_onboarding_1_view")
                    AdsConfig.nativeIntro1 = nativeAd

                    val adView = LayoutInflater.from(activity)
                        .inflate(R.layout.layout_native_media, null) as NativeAdView

                    binding!!.flAdPlaceholder.removeAllViews()
                    binding!!.flAdPlaceholder.addView(adView)
                    if (nativeAd != null) {
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                    }

                    binding!!.shimmerContainer.visibility = View.GONE
                    binding!!.loadingProgress.visibility = View.GONE

                    binding!!.flAdPlaceholder.postDelayed({
                        if (isAdded) {
                            binding!!.txtNext.visibility = View.VISIBLE
                        }
                    }, 500)
                }
            })
        } else {
            goneAds()
            binding!!.txtNext.visibility = View.VISIBLE
        }
    }

    private fun goneAds() {
        binding!!.flAdPlaceholder.removeAllViews()
        binding!!.frAds.visibility = View.GONE
        binding!!.loadingProgress.visibility = View.GONE
    }
}

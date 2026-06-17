package com.removedust.speaker.cleaner.presentation.ui.onboarding.fragment

import android.os.Handler
import android.os.Looper
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
        val activity = requireActivity()
        val adId = try {
           RemoteConfigManager.getInstance()
                .getAdId("native_onboarding_1", getString(R.string.native_onboarding_1))
        } catch (e: Exception) {
            getString(R.string.native_onboarding_1)
        }

        binding!!.txtNext.visibility = View.INVISIBLE
        binding!!.loadingProgress.visibility = View.VISIBLE
        binding!!.shimmerContainer.visibility = View.GONE

        if (adId.isNotEmpty()) {
            Admob.getInstance().loadNativeAd(activity, adId, object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    if (!isAdded) return
                    val adView = LayoutInflater.from(activity)
                        .inflate(R.layout.layout_native_media, null) as NativeAdView
                    binding!!.flAdPlaceholder.removeAllViews()
                    binding!!.flAdPlaceholder.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)

                    binding!!.shimmerContainer.visibility = View.GONE
                    binding!!.loadingProgress.visibility = View.GONE

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (isAdded) {
                            binding!!.txtNext.visibility = View.VISIBLE
                        }
                    }, 500)
                }

                override fun onAdFailedToLoad() {
                    if (!isAdded) return
                    binding!!.flAdPlaceholder.removeAllViews()
                    binding!!.frAds.visibility = View.GONE
                    binding!!.loadingProgress.visibility = View.GONE

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (isAdded) {
                            binding!!.txtNext.visibility = View.VISIBLE
                        }
                    }, 500)
                }
            })
        } else {
            binding!!.flAdPlaceholder.removeAllViews()
            binding!!.frAds.visibility = View.GONE
            binding!!.loadingProgress.visibility = View.GONE

            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) {
                    binding!!.txtNext.visibility = View.VISIBLE
                }
            }, 500)
        }
    }
}

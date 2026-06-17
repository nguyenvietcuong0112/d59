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
import com.removedust.speaker.cleaner.databinding.FragmentIntro2Binding
import com.removedust.speaker.cleaner.domain.remoteconfig.RemoteConfigManager
import com.removedust.speaker.cleaner.util.SharePreferenceUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentIntro2 : AbsBaseFragment<FragmentIntro2Binding?>() {

    override val layout: Int
        get() = R.layout.fragment_intro2

    override fun initView() {
        val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
        binding!!.txtNext.setOnClickListener(View.OnClickListener { view: View? ->
            viewPager.currentItem = viewPager.currentItem + 1
        })
        if (!SharePreferenceUtils.isOrganic(context)) {
            loadAds()
        } else {
            binding!!.frAds.visibility = View.GONE
        }
    }

    private fun loadAds() {
        val adId = try {
            RemoteConfigManager.getInstance()
                .getAdId("native_banner_ob", getString(R.string.native_banner_ob))
        } catch (e: Exception) {
            getString(R.string.native_banner_ob)
        }
        if (adId.isNotEmpty()) {
            Admob.getInstance().loadNativeAd(
                requireActivity(),
                adId,
                object : NativeCallback() {
                    override fun onAdFailedToLoad() {
                        super.onAdFailedToLoad()
                        if (!isAdded) return
                        binding!!.frAds.removeAllViews()
                        binding!!.frAds.visibility = View.GONE
                    }

                    override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                        super.onNativeAdLoaded(nativeAd)
                        if (!isAdded) return
                        val adView = LayoutInflater.from(requireActivity())
                            .inflate(R.layout.layout_native_no_media, null) as NativeAdView

                        binding!!.frAds.removeAllViews()
                        binding!!.frAds.addView(adView)
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                    }
                }
            )
        } else {
            binding!!.frAds.visibility = View.GONE
        }
    }
}

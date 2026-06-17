package com.removedust.speaker.cleaner.presentation.ui.onboarding.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

import com.mallegan.ads.callback.NativeCallback
import com.mallegan.ads.util.Admob
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.base.AbsBaseFragment
import com.removedust.speaker.cleaner.databinding.FragmentIntro4Binding
import com.removedust.speaker.cleaner.domain.remoteconfig.RemoteConfigManager
import com.removedust.speaker.cleaner.presentation.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentIntro4 : AbsBaseFragment<FragmentIntro4Binding?>() {

    override val layout: Int
        get() = R.layout.fragment_intro4

    override fun initView() {
        binding!!.txtNext.setOnClickListener(View.OnClickListener { v: View? -> navigateNextScreen() })
        loadAds()
    }

    private fun navigateNextScreen() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun loadAds() {
        val adId = try {
            RemoteConfigManager.getInstance()
                .getAdId("native_onboarding_4", getString(R.string.native_onboarding_4))
        } catch (e: Exception) {
            getString(R.string.native_onboarding_4)
        }
        Admob.getInstance().loadNativeAd(
            requireActivity(),
            adId,
            object : NativeCallback() {
                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    if (!isAdded) return
                    binding!!.frAds.removeAllViews()
                    binding!!.frAds.setVisibility(View.GONE)
                }

                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    if (!isAdded) return
                    val adView = LayoutInflater.from(requireActivity())
                        .inflate(R.layout.layout_native_media, null) as NativeAdView?

                    binding!!.frAds.removeAllViews()
                    binding!!.frAds.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                }
            })
    }
}

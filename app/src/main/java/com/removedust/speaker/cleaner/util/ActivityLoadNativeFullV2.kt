package com.removedust.speaker.cleaner.util

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.base.BaseActivity
import com.removedust.speaker.cleaner.databinding.ActivityNativeFullBinding
import com.removedust.speaker.cleaner.util.ActivityFullCallback
import com.mallegan.ads.callback.NativeCallback
import com.mallegan.ads.util.Admob
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ActivityLoadNativeFullV2 : BaseActivity() {
    var binding: ActivityNativeFullBinding? = null
    override fun bind() {
        SystemConfiguration.setStatusBarColor(
            this,
            R.color.transparent,
            SystemConfiguration.IconColor.ICON_DARK
        )
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater())
        setContentView(binding?.getRoot())

        val adId: String?
        if (getIntent().hasExtra(NATIVE_FUll_AD_ID)) {
            adId = getIntent().getStringExtra(NATIVE_FUll_AD_ID)
        } else {
            adId = getString("".toInt())
        }

        loadNativeFull(adId)
    }


    private fun loadNativeFull(adId: String?) {
        Admob.getInstance().loadNativeAds(this, adId, 1, object : NativeCallback() {
            override fun onAdFailedToLoad() {
                super.onAdFailedToLoad()
                binding?.frAdsFull?.setVisibility(View.GONE)
                if (callback != null) {
                    callback!!.onResultFromActivityFull()
                }
                finish()
            }

            override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                super.onNativeAdLoaded(nativeAd)
                val adView = LayoutInflater.from(this@ActivityLoadNativeFullV2)
                    .inflate(
                        R.layout.native_full_language,
                        null
                    ) as NativeAdView
                val closeButton = adView.findViewById<ImageView>(R.id.close)
                val mediaView =
                    adView.findViewById<MediaView>(R.id.ad_media)
                closeButton.setOnClickListener(View.OnClickListener { v: View? -> mediaView.performClick() })
                object : CountDownTimer(2000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        closeButton.setOnClickListener(View.OnClickListener { v: View? ->
                            if (callback != null) {
                                callback!!.onResultFromActivityFull()
                            }
                            finish()
                        })
                    }
                }.start()
                binding?.frAdsFull?.removeAllViews()
                binding?.frAdsFull?.addView(adView)
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
            }
        })
    }

    var count: Int = 0

    protected override fun onResume() {
        super.onResume()
        count++
        if (count >= 2) {
            if (callback != null) {
                callback!!.onResultFromActivityFull()
            }
            finish()
        }
    }

    companion object {
        const val NATIVE_FUll_AD_ID: String = "native_full_ad_id"

        private var callback: ActivityFullCallback? = null

        fun open(context: Context, id: String?, cb: ActivityFullCallback?) {
            callback = cb
            val intent = Intent(context, ActivityLoadNativeFullV2::class.java)
            intent.putExtra(NATIVE_FUll_AD_ID, id)
            context.startActivity(intent)
        }
    }
}
package com.removedust.speaker.cleaner.util

import androidx.appcompat.app.AppCompatActivity
import com.cscmobi.libraryads.ads.inter_ads.CSCInter
import com.google.android.gms.ads.nativead.NativeAd
import com.removedust.speaker.cleaner.R

object AdsConfig {

    fun showInterClickAd(activity: AppCompatActivity, onAdClosedAction: () -> Unit) {
        val isEnabled = RemoteConfigs.inter_click
        val interClickId = activity.getString(R.string.inter_click)

        CSCInter.loadAndShowInter(
            activity = activity,
            adId = interClickId,
            timeDelay = 0L,
            timeOut = 30000L,
            canShowId = isEnabled,
            nextAction = { _ ->
                onAdClosedAction()
            }
        )
    }
}

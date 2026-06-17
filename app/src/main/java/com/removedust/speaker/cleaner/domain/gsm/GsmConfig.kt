package com.removedust.speaker.cleaner.domain.gsm

import com.removedust.speaker.cleaner.BuildConfig

object GsmConfig {
    const val PROD_BASE_URL = "https://gsm.cscmobicorp.com/"
    const val DEV_BASE_URL = "https://gsmdev.cscmobicorp.com/"
    
    val CURRENT_BASE_URL: String
        get() = if (BuildConfig.DEBUG) DEV_BASE_URL else PROD_BASE_URL
    
    const val GSM_APP_ID = ""
}

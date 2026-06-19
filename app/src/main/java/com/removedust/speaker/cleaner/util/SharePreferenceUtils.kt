package com.removedust.speaker.cleaner.util

import android.content.Context

import com.mallegan.ads.util.Admob

object SharePreferenceUtils {
    private const val PREF_NAME = "speaker_cleaner_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    fun isOrganic(context: Context?): Boolean {
        return !Admob.getInstance().isLoadFullAds
//        return  false
    }



    fun getLanguage(context: Context?): String? {
        if (context == null) return null
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, null)
    }

    fun setLanguage(context: Context?, lang: String) {
        if (context == null) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }
}

package com.removedust.speaker.cleaner.util

import android.content.Context

object SharePreferenceUtils {
    private const val PREF_NAME = "speaker_cleaner_prefs"
    private const val KEY_IS_ORGANIC = "is_organic"
    private const val KEY_LANGUAGE = "selected_language"

    fun isOrganic(context: Context?): Boolean {
        if (context == null) return true
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_ORGANIC, true)
    }

    fun setOrganic(context: Context?, isOrganic: Boolean) {
        if (context == null) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_ORGANIC, isOrganic).apply()
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

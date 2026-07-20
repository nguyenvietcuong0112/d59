//package com.removedust.speaker.cleaner.presentation.ui.splash
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import androidx.appcompat.app.AppCompatDelegate
//import com.cscmobi.libraryads.ads.native_ads.model.LoadStrategy
//import com.cscmobi.libraryads.commons.sharepreference.CSCSPF
//import com.cscmobi.libraryads.data.AdsOBConfig
//import com.cscmobi.libraryads.data.AdsLanguageConfig
//import com.cscmobi.libraryads.data.AdsSplashConfig
//import com.cscmobi.libraryads.data.LanguageConfig
//import com.cscmobi.libraryads.data.LanguageSetting
//import com.cscmobi.libraryads.data.OBConfig
//import com.cscmobi.libraryads.data.OnActivityCallBack
//import com.cscmobi.libraryads.data.SplashConfig
//import com.cscmobi.libraryads.data.UiOBConfig
//import com.cscmobi.libraryads.data.UiLanguageConfig
//import com.cscmobi.libraryads.data.UiSplashConfig
//import com.cscmobi.libraryads.views.splash.CSCSplashActivity
//import com.removedust.speaker.cleaner.R
//import com.removedust.speaker.cleaner.presentation.ui.main.MainActivity
//import com.removedust.speaker.cleaner.util.EnumSelectLanguage
//import com.removedust.speaker.cleaner.util.SystemUtil
//
//import com.removedust.speaker.cleaner.util.SharePreferenceUtils
//
//@SuppressLint("CustomSplashScreen")
//class SplashActivity : CSCSplashActivity() {
//    override fun provideSplashConfig(): SplashConfig {
//        val isOrganic = SharePreferenceUtils.isOrganic(this)
//        val nativeFullId = if (isOrganic) "" else getString(R.string.native_splash_full)
//        val nativeFullHighId = if (isOrganic) "" else getString(R.string.native_splash_full_high)
//
//        return SplashConfig(
//            uiSplashConfig = UiSplashConfig(
//                resLayout = R.layout.activity_splash,
//                activityCallBack = object : OnActivityCallBack {
//                    override fun onNextActivity(inSession2: Boolean) {
//                        super.onNextActivity(inSession2)
//                     }
//                },
//                showFOForever = true,
//                homeActivity = MainActivity::class.java,
//                timeout = 45_000
//            ),
//            adsSplashConfig = AdsSplashConfig(
//                bannerId = getString(R.string.banner_splash),
//                interHighId = getString(R.string.inter_splash_high),
//                interAllId = getString(R.string.inter_splash),
//                nativeFullId = nativeFullId,
//                nativeFullHighId = nativeFullHighId,
//                nativeFullLayout = R.layout.layout_native_full,
//                admobAOAId = getString(R.string.resume_open_app),
//            )
//        )
//    }
//
//    override fun provideLanguageConfig(): LanguageConfig {
//        return LanguageConfig(
//            uiLanguageConfig = UiLanguageConfig(
//                resLayout = R.layout.activity_language_app,
//                itemLangDefault = R.layout.item_select_language_default,
//                itemLangSelected = R.layout.item_select_language_selected,
//                listLanguage = EnumSelectLanguage.toLanguageModelList(),
//                languageSetting = object : LanguageSetting {
//                    override fun onDone() {
//                        val code = CSCSPF(this@SplashActivity).language_code_selected
//                        SystemUtil.saveLocale(this@SplashActivity, code)
//                        AppCompatDelegate.setApplicationLocales(
//                            androidx.core.os.LocaleListCompat.forLanguageTags(code)
//                        )
//                        val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
//                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            putExtra("disable_animation", true)
//                        }
//                        startActivity(intent)
//                    }
//                }
//            ),
//            adsLanguageConfig = AdsLanguageConfig(
//                nativeLangId = getString(R.string.native_language),
//                nativeLangClickId = getString(R.string.native_language_click),
//                layoutNative = R.layout.layout_native_media,
//                layoutNativeClick = R.layout.layout_native_media
//            )
//        )
//    }
//
//    override fun provideOnboardConfig(): OBConfig {
//        val isOrganic = SharePreferenceUtils.isOrganic(this)
//        val nativeOB2Id = if (isOrganic) "" else getString(R.string.native_onboarding_2)
//        val nativeOB3Id = if (isOrganic) "" else getString(R.string.native_onboarding_3)
//        val nativeOBFull1Id = if (isOrganic) "" else getString(R.string.native_onboarding_full_1)
//        val nativeOBFull2Id = if (isOrganic) "" else getString(R.string.native_onboarding_full_2)
//
//        return OBConfig(
//            uiOBConfig = UiOBConfig(
//                resFragmentOB1 = R.layout.fragment_intro1,
//                resFragmentOB2 = R.layout.fragment_intro2,
//                resFragmentOB3 = R.layout.fragment_intro3,
//                resFragmentOB4 = R.layout.fragment_intro4,
//                resFragmentOBAdFull = R.layout.fragment_ob_ad_full,
//                activityCallback = object : OnActivityCallBack {
//                    override fun onNextActivity(inSession2: Boolean) {
//                        super.onNextActivity(inSession2)
//                        if (!inSession2) {
//                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
//                            startActivity(intent)
//                        }
//                    }
//                }
//            ),
//            adsOBConfig = AdsOBConfig(
//                nativeOB1Id = getString(R.string.native_onboarding_1),
//                nativeOB2Id = nativeOB2Id,
//                nativeOB3Id = nativeOB3Id,
//                nativeOB4Id = getString(R.string.native_onboarding_4),
//                nativeOBFull1Id = nativeOBFull1Id,
//                nativeOBFull2Id = nativeOBFull2Id,
//                layoutNativeOB1 = R.layout.layout_native_media,
//                layoutNativeOB2 = R.layout.layout_native_no_media,
//                layoutNativeOB3 = R.layout.layout_native_no_media,
//                layoutNativeOB4 = R.layout.layout_native_media,
//                layoutNativeFullOB = R.layout.admob_layout_native_full,
//            )
//        )
//    }
//}
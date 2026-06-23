package com.removedust.speaker.cleaner


import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.removedust.speaker.cleaner.domain.adjust.AppAdjustTokens
import com.removedust.speaker.cleaner.domain.gsm.GsmManager
import com.removedust.speaker.cleaner.domain.remoteconfig.getRemoteAdId
import com.removedust.speaker.cleaner.util.SharePreferenceUtils
import com.removedust.speaker.cleaner.util.SystemUtil
import com.facebook.FacebookSdk
import com.google.firebase.FirebaseApp

import com.mallegan.ads.util.AdjustHelper
import com.mallegan.ads.util.AdsApplication
import com.mallegan.ads.util.AppOpenManager
import com.mallegan.ads.util.PreferenceManager
import com.removedust.speaker.cleaner.data.datasource.AudioDatasource
import com.removedust.speaker.cleaner.data.repository.SpeakerCleanerRepositoryImpl
import com.removedust.speaker.cleaner.domain.repository.SpeakerCleanerRepository
import com.removedust.speaker.cleaner.domain.usecase.*
import com.removedust.speaker.cleaner.domain.gsm.GsmApiService
import com.removedust.speaker.cleaner.domain.gsm.GsmAuthInterceptor
import com.removedust.speaker.cleaner.domain.gsm.GsmConfig
import com.removedust.speaker.cleaner.presentation.ui.language.LanguageActivity
import com.removedust.speaker.cleaner.presentation.ui.main.MainActivity
import com.removedust.speaker.cleaner.presentation.ui.onboarding.OnboardActitivty
import com.removedust.speaker.cleaner.presentation.ui.splash.SplashActivity
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : AdsApplication() {

    lateinit var gsmManager: GsmManager
        private set
    lateinit var audioDatasource: AudioDatasource
        private set
    lateinit var repository: SpeakerCleanerRepository
        private set

    lateinit var autoCleaningUseCase: AutoCleaningUseCase
        private set
    lateinit var manualCleaningUseCase: ManualCleaningUseCase
        private set
    lateinit var headphoneCleaningUseCase: HeadphoneCleaningUseCase
        private set
    lateinit var soundFixUseCase: SoundFixUseCase
        private set
    lateinit var stopCleaningUseCase: StopCleaningUseCase
        private set

    companion object {
        lateinit var instance: Application
            private set
    }

    override fun enableAdsResume(): Boolean = true
    override fun getListTestDeviceId(): List<String>? = null
    override fun getResumeAdId(): String {
        return getRemoteAdId("resume_open_app", R.string.resume_open_app)
    }

    override fun buildDebug(): Boolean? = null

    override fun onCreate() {
        instance = this

        // Initialize dependencies manually
        audioDatasource = AudioDatasource()
        repository = SpeakerCleanerRepositoryImpl(audioDatasource)

        autoCleaningUseCase = AutoCleaningUseCase(repository)
        manualCleaningUseCase = ManualCleaningUseCase(repository)
        headphoneCleaningUseCase = HeadphoneCleaningUseCase(repository)
        soundFixUseCase = SoundFixUseCase(repository)
        stopCleaningUseCase = StopCleaningUseCase(repository)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(GsmAuthInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(GsmConfig.CURRENT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val gsmApiService = retrofit.create(GsmApiService::class.java)
        gsmManager = GsmManager(this, gsmApiService)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
//        AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity::class.java)
        AppOpenManager.getInstance().disableAppResumeWithActivity(LanguageActivity::class.java)
        AppOpenManager.getInstance().disableAppResumeWithActivity(OnboardActitivty::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            delay(5000)
            gsmManager.loginGSM()
        }

        val savedLang = SharePreferenceUtils.getLanguage(this)
        if (savedLang == null) {
            SharePreferenceUtils.setLanguage(this, "English")
            SystemUtil.saveLocale(this, "en")
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        } else {
            val localeCode = when (savedLang) {
                "Arabic", "ar" -> "ar"
                "English", "en" -> "en"
                "French", "fr" -> "fr"
                "German", "de" -> "de"
                "Hindi", "hi" -> "hi"
                "Indonesian", "id" -> "id"
                "Italian", "it" -> "it"
                "Japanese", "ja" -> "ja"
                "Portuguese", "pt" -> "pt"
                "Russian", "ru" -> "ru"
                "Spanish", "es" -> "es"
                "Thai", "th" -> "th"
                "Turkish", "tr" -> "tr"
                "Urdu", "ur" -> "ur"
                "Vietnamese", "vi" -> "vi"
                else -> "en"
            }
            SystemUtil.saveLocale(
                this,
                localeCode
            )
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            if (currentLocales.isEmpty || currentLocales.get(0)?.language != localeCode) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeCode))
            }
        }

        Executors.newSingleThreadExecutor().execute {
            FirebaseApp.initializeApp(this)
            FacebookSdk.setClientToken(getString(R.string.facebook_client_token))
            AdjustHelper.init(
                application = this,
                appToken = AppAdjustTokens.ADJUST_APP_TOKEN,
                iapEventToken = AppAdjustTokens.EVENT_IAP_COMMON,
                isDebug = BuildConfig.DEBUG
            )
        }

    }
}

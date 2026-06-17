package com.removedust.speaker.cleaner.presentation.ui.language

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.card.MaterialCardView
import com.mallegan.ads.callback.NativeCallback
import com.mallegan.ads.util.Admob
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.base.BaseActivity
import com.removedust.speaker.cleaner.databinding.ActivityLanguageBinding
import com.removedust.speaker.cleaner.domain.remoteconfig.RemoteConfigManager
import com.removedust.speaker.cleaner.presentation.ui.onboarding.OnboardActitivty
import com.removedust.speaker.cleaner.util.AdsConfig
import com.removedust.speaker.cleaner.util.SharePreferenceUtils
import com.removedust.speaker.cleaner.util.SystemUtil
import dagger.hilt.android.AndroidEntryPoint

class LanguageActivity : BaseActivity() {

    companion object {
        const val EXTRA_FROM_PROFILE = "extra_from_profile"
    }

    private lateinit var binding: ActivityLanguageBinding
    private lateinit var adapter: LanguageSelectionListAdapter
    private var selectedLanguage = ""
    private var isLanguageSelected = false

    private val languages = listOf(
        LanguageModel("en", "English", R.drawable.flag_us),
        LanguageModel("fr", "French", R.drawable.flag_fr),
        LanguageModel("de", "Deutsch", R.drawable.flag_de),
        LanguageModel("hi", "Hindi", R.drawable.flag_in),
        LanguageModel("id", "Indonesian", R.drawable.flag_id),
        LanguageModel("pt", "Portuguese", R.drawable.flag_pt),
        LanguageModel("es", "Espanol", R.drawable.flag_es),
        LanguageModel("vi", "Tieng Viet", R.drawable.flag_vn),
        LanguageModel("ja", "Japanese", R.drawable.flag_jp)
    )

    override fun bind() {
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show back button if opened from ProfileFragment
        val isFromProfile = intent.getBooleanExtra(EXTRA_FROM_PROFILE, false)
        if (isFromProfile) {
            binding.ivBack.visibility = View.VISIBLE
            binding.ivBack.setOnClickListener { finish() }

            // Pre-select the currently active language if coming from profile
            val savedLanguage = SharePreferenceUtils.getLanguage(this) ?: "en"
            selectedLanguage = if (savedLanguage.length > 2) {
                when (savedLanguage) {
                    "French" -> "fr"
                    "German", "Deutsch" -> "de"
                    "Hindi" -> "hi"
                    "Indonesian" -> "id"
                    "Portuguese" -> "pt"
                    "Spanish", "Espanol" -> "es"
                    "Vietnamese", "Tieng Viet" -> "vi"
                    "Japanese" -> "ja"
                    else -> "en"
                }
            } else {
                savedLanguage
            }
            isLanguageSelected = true
            binding.ivSelect.alpha = 1.0f
        }

        setupRecyclerView()
        setupListeners()
        loadAds()
        nativeIntro1()
    }

    private fun setupRecyclerView() {
        adapter = LanguageSelectionListAdapter(
            onItemClick = { lang ->
                selectedLanguage = lang.code
                
                // 1. Save selected language immediately
                SharePreferenceUtils.setLanguage(this@LanguageActivity, lang.code)

                SystemUtil.saveLocale(this@LanguageActivity, lang.code)

                // Refresh checked indicators in lists
                adapter.notifyDataSetChanged()

                // 2. Make button active visually and load select ad
                binding.ivSelect.alpha = 1.0f
                isLanguageSelected = true
                loadAdsNativeLanguageSelect()
            },
            isSelectedPredicate = { lang ->
                lang.code == selectedLanguage
            }
        )

        binding.uiLanguage.layoutManager = LinearLayoutManager(this)
        binding.uiLanguage.adapter = adapter
        adapter.submitList(languages)
    }

    private fun setupListeners() {
        binding.frNext.setOnClickListener {
            if (!isLanguageSelected) {
                Toast.makeText(this, getString(R.string.toast_select_language_continue), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Do not progress if ad is currently loading
            if (binding.icLoading.visibility == View.VISIBLE) {
                return@setOnClickListener
            }

            // Apply selected application locale dynamically on transition
            val localeCode = selectedLanguage
            SystemUtil.saveLocale(this, localeCode)
            SystemUtil.changeLang(localeCode, this)
            val appLocale = LocaleListCompat.forLanguageTags(localeCode)
            AppCompatDelegate.setApplicationLocales(appLocale)

            val isFromProfile = intent.getBooleanExtra(EXTRA_FROM_PROFILE, false)
            if (isFromProfile) {
                finish()
            } else {
                val intent = Intent(this, OnboardActitivty::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun loadAds() {
        val isVIP = false
        if (isVIP) {
            binding.frAds.visibility = View.GONE
            checkNextButtonStatus(true)
            return
        }
        checkNextButtonStatus(false)
        val adId = try {
            RemoteConfigManager.getInstance()
                .getAdId("native_language", getString(R.string.native_language))
        } catch (e: Exception) {
            getString(R.string.native_language)
        }
        if (adId.isNotEmpty()) {
            Admob.getInstance().loadNativeAds(this, adId, 1, object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    val adView = LayoutInflater.from(this@LanguageActivity)
                        .inflate(R.layout.layout_native_media, null) as NativeAdView
                    binding.frAds.removeAllViews()
                    binding.frAds.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                    checkNextButtonStatus(true)
                }

                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    binding.frAds.removeAllViews()
                    binding.frAds.visibility = View.GONE
                    checkNextButtonStatus(true)
                }
            })
        } else {
            binding.frAds.removeAllViews()
            binding.frAds.visibility = View.GONE
            checkNextButtonStatus(true)
        }
    }

    private fun nativeIntro1() {
        val isVIP = false
        if (isVIP) {
            AdsConfig.nativeIntro1 = null
            return
        }
        val adId = try {
            RemoteConfigManager.getInstance()
                .getAdId("native_onboarding_1", getString(R.string.native_onboarding_1))
        } catch (e: Exception) {
            getString(R.string.native_onboarding_1)
        }
        Admob.getInstance().loadNativeAd(
            this,
            adId,
            object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    AdsConfig.nativeIntro1 = nativeAd
                }

                override fun onAdFailedToLoad() {
                    AdsConfig.nativeIntro1 = null
                }
            }
        )
    }

    private fun loadAdsNativeLanguageSelect() {
        val isVIP = false
        if (isVIP) {
            binding.frAds.visibility = View.GONE
            checkNextButtonStatus(true)
            return
        }
        checkNextButtonStatus(false)
        val adId = try {
            RemoteConfigManager.getInstance()
                .getAdId("native_language_click", getString(R.string.native_language_click))
        } catch (e: Exception) {
            getString(R.string.native_language_click)
        }
        if (adId.isNotEmpty()) {
            Admob.getInstance().loadNativeAds(this, adId, 1, object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    val adView = LayoutInflater.from(this@LanguageActivity)
                        .inflate(R.layout.layout_native_media, null) as NativeAdView
                    binding.frAds.removeAllViews()
                    binding.frAds.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                    checkNextButtonStatus(true)
                }

                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    binding.frAds.removeAllViews()
                    checkNextButtonStatus(true)
                }
            })
        } else {
            binding.frAds.removeAllViews()
            checkNextButtonStatus(true)
        }
    }

    private fun checkNextButtonStatus(isReady: Boolean) {
        if (isReady) {
            binding.ivSelect.visibility = View.VISIBLE
            binding.icLoading.visibility = View.GONE
        } else {
            binding.ivSelect.visibility = View.GONE
            binding.icLoading.visibility = View.VISIBLE
        }
    }

}

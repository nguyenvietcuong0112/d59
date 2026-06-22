package com.removedust.speaker.cleaner.presentation.ui.main

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.removedust.speaker.cleaner.base.BaseActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.databinding.ActivityMainBinding
import com.removedust.speaker.cleaner.presentation.ui.settings.SettingsActivity
import com.removedust.speaker.cleaner.util.AdsConfig
import android.view.LayoutInflater
import android.widget.ImageView
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mallegan.ads.callback.NativeCallback
import com.mallegan.ads.util.Admob
import com.removedust.speaker.cleaner.domain.remoteconfig.RemoteConfigManager
import com.removedust.speaker.cleaner.util.SharePreferenceUtils
import com.removedust.speaker.cleaner.util.LogEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeFragment: HomeFragment
    private lateinit var tipsFragment: TipsFragment
    private lateinit var activeFragment: Fragment
    private var savedInstanceState: Bundle? = null

    private val handlerADS = Handler(Looper.getMainLooper())
    private var isFirstLoad = true
    private var delayedLoadExpandTask: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
        super.onCreate(savedInstanceState)
    }

    override fun bind() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        LogEvent.log(this, "main_view")

        binding.frAdsBanner.bringToFront()
        binding.frAdsCollap.bringToFront()

        setupNavigation(savedInstanceState)
        setupListeners()
        showPopupExit()
    }

    private fun showPopupExit() {
        onBackPressedDispatcher.addCallback(
            this,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmDialog()
                }
            })
    }

    private fun showExitConfirmDialog() {
        if (isFinishing || isDestroyed) return
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_exit_confirm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<View>(R.id.btnCancel)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.btnExit)?.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }

        dialog.show()
    }

    private fun setupNavigation(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            homeFragment = HomeFragment()
            tipsFragment = TipsFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.layout_content_container, homeFragment, "home")
                .add(R.id.layout_content_container, tipsFragment, "tips").hide(tipsFragment)
                .commit()
            activeFragment = homeFragment
        } else {
            homeFragment =
                supportFragmentManager.findFragmentByTag("home") as? HomeFragment ?: HomeFragment()
            tipsFragment =
                supportFragmentManager.findFragmentByTag("tips") as? TipsFragment ?: TipsFragment()
            activeFragment = if (tipsFragment.isVisible) tipsFragment else homeFragment
        }

        binding.btnNavHome.setOnClickListener {
            if (activeFragment != homeFragment) {
                AdsConfig.showInterClickAd(this) {
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(homeFragment)
                        .commit()
                    activeFragment = homeFragment
                    setNavTabActive(isHome = true)
                }
            } else {
                setNavTabActive(isHome = true)
            }
        }

        binding.btnNavTips.setOnClickListener {
            if (activeFragment != tipsFragment) {
                AdsConfig.showInterClickAd(this) {
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(tipsFragment)
                        .commit()
                    activeFragment = tipsFragment
                    setNavTabActive(isHome = false)
                }
            } else {
                setNavTabActive(isHome = false)
            }
        }

        // Set initial active tab visual state
        setNavTabActive(isHome = (activeFragment == homeFragment))
    }

    private fun setupListeners() {
        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setNavTabActive(isHome: Boolean) {
        val activeColor = ContextCompat.getColor(this, R.color.primary_blue_selected)
        val inactiveColor = ContextCompat.getColor(this, R.color.black)

        if (isHome) {
            binding.ivNavHome.setImageResource(R.drawable.ic_home_selected)
            binding.ivNavHome.imageTintList = null
            binding.tvNavHome.setTextColor(activeColor)

            binding.ivNavTips.setImageResource(R.drawable.ic_tips_unselected)
            binding.ivNavTips.imageTintList = null
            binding.tvNavTips.setTextColor(inactiveColor)
        } else {
            binding.ivNavHome.setImageResource(R.drawable.ic_home_unselected)
            binding.ivNavHome.imageTintList = null
            binding.tvNavHome.setTextColor(inactiveColor)

            binding.ivNavTips.setImageResource(R.drawable.ic_tips_selected)
            binding.ivNavTips.imageTintList = null
            binding.tvNavTips.setTextColor(activeColor)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!SharePreferenceUtils.isOrganic(this)) {
            if (isFirstLoad) {
                loadNativeBanner {
                    delayedLoadExpandTask = Runnable {
                        loadNativeCollapse()
                        isFirstLoad = false
                    }
                    handlerADS.postDelayed(delayedLoadExpandTask!!, 1000)
                }
            } else {
                loadNativeBanner {
                    delayedLoadExpandTask = Runnable {
                        loadNativeCollapse()
                    }
                    handlerADS.postDelayed(delayedLoadExpandTask!!, 45000)
                }
            }
        } else {
            binding.frAdsCollap.removeAllViews()
            binding.frAdsBanner.removeAllViews()
        }
    }

    override fun onPause() {
        super.onPause()
        delayedLoadExpandTask?.let {
            handlerADS.removeCallbacks(it)
            delayedLoadExpandTask = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerADS.removeCallbacksAndMessages(null)
    }

    private fun loadNativeCollapse() {
        val nativeAllId = try {
            RemoteConfigManager.getInstance()
                .getAdId("native_collab_home", getString(R.string.native_collab_home))
        } catch (e: Exception) {
            getString(R.string.native_collab_home)
        }

        if (nativeAllId.isNotEmpty()) {
            Admob.getInstance().loadNativeAd(this, nativeAllId, object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    if (isDestroyed || isFinishing) return

                    val adView = LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.layout_native_home_collapse, null) as NativeAdView

                    binding.layoutBottomNav.visibility = View.GONE

                    binding.frAdsCollap.removeAllViews()

                    val closeButton = adView.findViewById<ImageView>(R.id.close)

                    closeButton?.setOnClickListener {
                        binding.frAdsCollap.removeAllViews()
                        binding.layoutBottomNav.visibility = View.VISIBLE
                        loadNativeBanner()
                    }

                    binding.frAdsCollap.addView(adView)
                    binding.frAdsCollap.bringToFront()
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                }

                override fun onAdFailedToLoad() {
                    if (isDestroyed || isFinishing) return
                    binding.frAdsCollap.removeAllViews()
                    binding.layoutBottomNav.visibility = View.VISIBLE
                }
            })
        } else {
            binding.frAdsCollap.removeAllViews()
            binding.layoutBottomNav.visibility = View.VISIBLE
        }
    }

    private fun loadNativeBanner(onLoaded: (() -> Unit)? = null) {
        binding.frAdsCollap.removeAllViews()

        val nativeAllId = try {
            RemoteConfigManager.getInstance()
                .getAdId("native_banner_home", getString(R.string.native_banner_home))
        } catch (e: Exception) {
            getString(R.string.native_banner_home)
        }

        if (nativeAllId.isNotEmpty()) {
            Admob.getInstance().loadNativeAd(this, nativeAllId, object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    if (isDestroyed || isFinishing) return

                    val adView = LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.layout_native_banner, null) as NativeAdView

                    binding.layoutBottomNav.visibility = View.VISIBLE

                    binding.frAdsBanner.removeAllViews()
                    binding.frAdsBanner.addView(adView)
                    binding.frAdsBanner.bringToFront()

                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)

                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad() {
                    if (isDestroyed || isFinishing) return
                    binding.frAdsBanner.removeAllViews()
                    binding.layoutBottomNav.visibility = View.VISIBLE
                    onLoaded?.invoke()
                }
            })
        } else {
            binding.frAdsBanner.removeAllViews()
            binding.layoutBottomNav.visibility = View.VISIBLE
            onLoaded?.invoke()
        }
    }
}

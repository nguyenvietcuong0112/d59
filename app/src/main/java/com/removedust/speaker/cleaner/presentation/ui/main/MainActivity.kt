package com.removedust.speaker.cleaner.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.removedust.speaker.cleaner.base.BaseActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.databinding.ActivityMainBinding
import com.removedust.speaker.cleaner.presentation.ui.settings.SettingsActivity
import com.removedust.speaker.cleaner.util.AdsConfig
import android.os.Handler
import android.os.Looper
import com.removedust.speaker.cleaner.util.RemoteConfigs
import com.cscmobi.libraryads.ads.native_ads.CSCNativeManager

import com.removedust.speaker.cleaner.util.LogEvent
import com.removedust.speaker.cleaner.util.SharePreferenceUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeFragment: HomeFragment
    private lateinit var tipsFragment: TipsFragment
    private lateinit var activeFragment: Fragment
    private var savedInstanceState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (intent.getBooleanExtra("disable_animation", false)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            }
        }
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
            activeFragment = if (!tipsFragment.isHidden) tipsFragment else homeFragment
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
        loadNativeBanner()
        loadNativeCollapse()

    }


    private fun loadNativeCollapse() {
        val isEnabled = RemoteConfigs.native_collab_home

        CSCNativeManager.showCollapsibleNative(
            activity = this,
            adFrame = binding.frAdsCollap,
            adName = "native_collab_home",
            adId = getString(R.string.native_collab_home),
            adLayout = R.layout.layout_native_home_collapse,
            canShowAd = isEnabled,
            refreshTime = 20_000L
        )
    }

    private fun loadNativeBanner() {
        val isEnabled = RemoteConfigs.native_banner_home

        CSCNativeManager.showNative(
            adFrame = binding.frAdsBanner,
            adName = "native_banner_home",
            adId = getString(R.string.native_banner_home),
            adLayout = R.layout.layout_native_banner,
            canShowAd = isEnabled,
        )
    }
}

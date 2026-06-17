package com.removedust.speaker.cleaner.presentation.ui.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeFragment: HomeFragment
    private lateinit var tipsFragment: TipsFragment
    private lateinit var activeFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this

        setupNavigation(savedInstanceState)
        setupListeners()
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
            homeFragment = supportFragmentManager.findFragmentByTag("home") as? HomeFragment ?: HomeFragment()
            tipsFragment = supportFragmentManager.findFragmentByTag("tips") as? TipsFragment ?: TipsFragment()
            activeFragment = if (tipsFragment.isVisible) tipsFragment else homeFragment
        }

        binding.btnNavHome.setOnClickListener {
            if (activeFragment != homeFragment) {
                supportFragmentManager.beginTransaction()
                    .hide(activeFragment)
                    .show(homeFragment)
                    .commit()
                activeFragment = homeFragment
            }
            setNavTabActive(isHome = true)
        }

        binding.btnNavTips.setOnClickListener {
            if (activeFragment != tipsFragment) {
                supportFragmentManager.beginTransaction()
                    .hide(activeFragment)
                    .show(tipsFragment)
                    .commit()
                activeFragment = tipsFragment
            }
            setNavTabActive(isHome = false)
        }

        // Set initial active tab visual state
        setNavTabActive(isHome = (activeFragment == homeFragment))
    }

    private fun setupListeners() {
        binding.btnSettings.setOnClickListener {
            Toast.makeText(this, "Cấu hình - Ứng dụng đã được tối ưu tần số.", Toast.LENGTH_SHORT).show()
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
}

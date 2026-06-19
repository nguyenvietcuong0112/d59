package com.removedust.speaker.cleaner.presentation.ui.onboarding

import android.view.MotionEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.base.BaseActivity
import com.removedust.speaker.cleaner.databinding.ActivityIntroSlideshowBinding

import com.removedust.speaker.cleaner.presentation.ui.onboarding.fragment.FragmentIntro1
import com.removedust.speaker.cleaner.presentation.ui.onboarding.fragment.FragmentIntro12ads
import com.removedust.speaker.cleaner.presentation.ui.onboarding.fragment.FragmentIntro2
import com.removedust.speaker.cleaner.presentation.ui.onboarding.fragment.FragmentIntro3
import com.removedust.speaker.cleaner.presentation.ui.onboarding.fragment.FragmentIntro34ads
import com.removedust.speaker.cleaner.presentation.ui.onboarding.fragment.FragmentIntro4
import com.removedust.speaker.cleaner.util.SharePreferenceUtils
import com.removedust.speaker.cleaner.util.SystemConfiguration
import com.removedust.speaker.cleaner.util.SystemUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardActitivty : BaseActivity() {

    private lateinit var binding: ActivityIntroSlideshowBinding

    override fun bind() {
        SystemUtil.setLocale(this)
        SystemConfiguration.setStatusBarColor(
            this,
            R.color.transparent,
            SystemConfiguration.IconColor.ICON_LIGHT
        )

        binding = ActivityIntroSlideshowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                }
            }
        )

        val fragmentList = ArrayList<Fragment>()

        fragmentList.add(FragmentIntro1())
        if (!SharePreferenceUtils.isOrganic(this)) {
            fragmentList.add(FragmentIntro12ads())
        }
        fragmentList.add(FragmentIntro2())
        fragmentList.add(FragmentIntro3())
        if (!SharePreferenceUtils.isOrganic(this)) {
            fragmentList.add(FragmentIntro34ads())
        }
        fragmentList.add(FragmentIntro4())

        val adapter = ViewIntroAdapter(
            this,
            fragmentList,
            supportFragmentManager,
            lifecycle
        )

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 2

        val recyclerView = binding.viewPager.getChildAt(0) as RecyclerView
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            private var initialX = 0f
            private var hasInitialX = false

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = e.x
                        hasInitialX = true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!hasInitialX) {
                            initialX = e.x
                            hasInitialX = true
                        }
                        val currentX = e.x
                        val diff = currentX - initialX
                        if (diff > 10) {
                            return true
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        hasInitialX = false
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })


    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

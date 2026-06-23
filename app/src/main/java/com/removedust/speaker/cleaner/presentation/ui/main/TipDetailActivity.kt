package com.removedust.speaker.cleaner.presentation.ui.main

import android.os.Bundle
import com.removedust.speaker.cleaner.base.BaseActivity
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.databinding.ActivityTipDetailBinding
import com.removedust.speaker.cleaner.util.AdsConfig
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TipDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityTipDetailBinding

    override fun bind() {
        binding = ActivityTipDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get optional passed extras, defaulting to standard strings if not passed
        val title = intent.getStringExtra("extra_title") ?: ""
        val date = intent.getStringExtra("extra_date") ?: "12 Jun, 2016"
        val content = intent.getStringExtra("extra_content")
        val imageResId = intent.getIntExtra("extra_image", R.drawable.bg_tip_placeholder)

        binding.tvTitle.text = title
        binding.tvDate.text = date
        if (content != null) {
            binding.tvContent.text = content
        }
        binding.ivTipImage.setImageResource(imageResId)

        binding.btnBack.setOnClickListener {
            navigateBack()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateBack()
                }
            })
    }

    private fun navigateBack() {
        AdsConfig.showInterClickAd(this) {
            finish()
        }
    }
}

package com.removedust.speaker.cleaner.presentation.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.removedust.speaker.cleaner.BuildConfig
import com.removedust.speaker.cleaner.base.BaseActivity
import com.removedust.speaker.cleaner.databinding.ActivitySettingsBinding
import com.removedust.speaker.cleaner.presentation.ui.language.LanguageActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun bind() {
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }


        binding.btnSettingsLanguage.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java).apply {
                putExtra(LanguageActivity.EXTRA_FROM_PROFILE, true)
            }
            startActivity(intent)
        }

        binding.btnSettingsRate.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                startActivity(intent)
            }
        }

        binding.btnSettingsShare.setOnClickListener {
            val shareText = "Check out this amazing Speaker Cleaner app! Download it here: https://play.google.com/store/apps/details?id=$packageName"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share app via"))
        }

        binding.btnSettingsPolicy.setOnClickListener {
            val url = "https://docs.google.com/document/d/1JUD-uBWf7Nd-aGjyydJVwrg4nJOwW5XT/edit"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.selector = Intent(Intent.ACTION_VIEW, Uri.parse("https://"))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(fallbackIntent)
            }
        }

        binding.tvSettingsVersionValue.text = "v${BuildConfig.VERSION_NAME}"
    }


}

package com.removedust.speaker.cleaner.presentation.ui.manual

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.removedust.speaker.cleaner.base.BaseActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.databinding.ActivityManualCleanBinding
import com.removedust.speaker.cleaner.presentation.state.CleaningState
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import com.removedust.speaker.cleaner.presentation.ui.main.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.removedust.speaker.cleaner.util.showVolumeWarningDialog
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mallegan.ads.callback.NativeCallback
import com.mallegan.ads.util.Admob
import com.removedust.speaker.cleaner.domain.remoteconfig.RemoteConfigManager

import com.removedust.speaker.cleaner.util.AdsConfig

@AndroidEntryPoint
class ManualCleanActivity : BaseActivity() {

    private lateinit var binding: ActivityManualCleanBinding
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun bind() {
        binding = ActivityManualCleanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this


        setupListeners()
        observeViewModel()
        loadAdsNative()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            viewModel.stopCleaning()
            finish()
        }

        binding.btnManualStartStop.setOnClickListener {
            val state = viewModel.uiState.value.state
            if (state is CleaningState.Cleaning) {
                viewModel.stopCleaning()
            } else {
                checkVolumeAndRun {
                    AdsConfig.showInterClickAd(this) {
                        val freq = binding.frequencySlider.getFrequency()
                        viewModel.startManualCleaning(freq)
                    }
                }
            }
        }

        binding.frequencySlider.setOnFrequencyChangedListener { freq ->
            binding.tvManualFrequency.text = getString(R.string.text_frequency, freq)
            val state = viewModel.uiState.value.state
            if (state is CleaningState.Cleaning) {
                viewModel.updateManualFrequency(freq)
            }
        }

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    updateUI(uiState)
                }
            }
        }
    }

    private fun updateUI(uiState: CleanupUIState) {
        binding.tvManualFrequency.text = getString(R.string.text_frequency, binding.frequencySlider.getFrequency())

        when (val state = uiState.state) {
            is CleaningState.Idle -> {
                binding.tvManualState.text = getString(R.string.state_ready)

                binding.lottieGlow.visibility = View.INVISIBLE
                binding.lottieGlow.cancelAnimation()
                binding.viewActiveBlueRing.visibility = View.INVISIBLE
                binding.btnManualStartStop.text = getString(R.string.btn_start)
                binding.btnManualStartStop.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(this, R.color.primary_blue_selected)
                )
                binding.frequencySlider.setEnabled(true)
            }
            is CleaningState.Cleaning -> {
                binding.tvManualState.text = getString(R.string.state_cleaning)
                binding.tvManualFrequency.text = getString(R.string.text_frequency, uiState.currentFrequency)
                binding.frequencySlider.setFrequency(uiState.currentFrequency)
                binding.lottieGlow.visibility = View.VISIBLE
                binding.lottieGlow.playAnimation()
                binding.viewActiveBlueRing.visibility = View.VISIBLE
                binding.btnManualStartStop.text = getString(R.string.btn_stop)
                binding.btnManualStartStop.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(this, R.color.error)
                )
            }
            is CleaningState.Complete -> {
                viewModel.stopCleaning()
            }
            is CleaningState.Error -> {
                viewModel.stopCleaning()
                Toast.makeText(this, getString(R.string.state_error, state.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkVolumeAndRun(action: () -> Unit) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        if (currentVolume < maxVolume) {
            showVolumeWarningDialog {
                action()
            }
        } else {
            action()
        }
    }

    private fun loadAdsNative() {
        val adId = try {
            RemoteConfigManager.getInstance()
                .getAdId("native_all", getString(R.string.native_all))
        } catch (e: Exception) {
            getString(R.string.native_all)
        }
        if (adId.isNotEmpty()) {
            Admob.getInstance().loadNativeAds(this, adId, 1, object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd?) {
                    super.onNativeAdLoaded(nativeAd)
                    val adView = LayoutInflater.from(this@ManualCleanActivity)
                        .inflate(R.layout.layout_native_media, null) as NativeAdView
                    binding.frAds.removeAllViews()
                    binding.frAds.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                }

                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    binding.frAds.removeAllViews()
                    binding.frAds.visibility = View.GONE
                }
            })
        } else {
            binding.frAds.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopCleaning()
    }
}

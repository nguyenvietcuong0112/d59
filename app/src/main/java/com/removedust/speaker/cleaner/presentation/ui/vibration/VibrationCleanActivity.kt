package com.removedust.speaker.cleaner.presentation.ui.vibration

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.viewModels
import com.removedust.speaker.cleaner.base.BaseActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.databinding.ActivityVibrationCleanBinding
import com.removedust.speaker.cleaner.presentation.state.CleaningState
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import com.removedust.speaker.cleaner.presentation.ui.main.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.removedust.speaker.cleaner.util.showVolumeWarningDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mallegan.ads.callback.NativeCallback
import com.mallegan.ads.util.Admob
import com.removedust.speaker.cleaner.domain.remoteconfig.RemoteConfigManager
import com.removedust.speaker.cleaner.presentation.ui.testspeaker.TestSpeakerActivity

import com.removedust.speaker.cleaner.util.AdsConfig

@AndroidEntryPoint
class VibrationCleanActivity : BaseActivity() {

    private lateinit var binding: ActivityVibrationCleanBinding
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    private var isStrongMode = false
    private var isVibrating = false
    private var progressJob: Job? = null

    override fun bind() {
        binding = ActivityVibrationCleanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this


        updateSelectorUI()
        setupListeners()
        observeViewModel()
        loadAdsNative()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            stopVibrationCleaning()
            finish()
        }

        binding.cardModeNormal.setOnClickListener {
            if (!isVibrating) {
                isStrongMode = false
                updateSelectorUI()
            }
        }

        binding.cardModeStrong.setOnClickListener {
            if (!isVibrating) {
                isStrongMode = true
                updateSelectorUI()
            }
        }

        binding.btnVibrationStartStop.setOnClickListener {
            if (isVibrating) {
                stopVibrationCleaning()
            } else {
                checkVolumeAndRun {
                    AdsConfig.showInterClickAd(this) {
                        startVibrationCleaning()
                    }
                }
            }
        }
    }

    private fun updateSelectorUI() {
        binding.cardModeNormal.isSelected = !isStrongMode
        binding.cardModeStrong.isSelected = isStrongMode
    }

    private fun startVibrationCleaning() {
        isVibrating = true
        binding.viewActiveBlueRing.visibility = android.view.View.VISIBLE
        binding.lottieGlow.visibility = android.view.View.VISIBLE
        binding.lottieGlow.playAnimation()
        binding.btnVibrationStartStop.text = "Stop"
        binding.btnVibrationStartStop.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.error)
        )

        // Disable mode selectors
        binding.cardModeNormal.isEnabled = false
        binding.cardModeStrong.isEnabled = false

        // 1. Play low frequency vibrating sound sweep (e.g. 50Hz for Strong, 65Hz for Normal)
        viewModel.startManualCleaning(if (isStrongMode) 50 else 65)

        // 2. Trigger hardware device vibration
        startHardwareVibrate(isStrongMode)

        // 3. Animate circular percentage count (runs for 30s)
        progressJob?.cancel()
        progressJob = lifecycleScope.launch {
            val totalSeconds = 30
            val totalTicks = totalSeconds * 10
            for (tick in 0..totalTicks) {
                val percent = (tick * 100) / totalTicks
                binding.tvVibrationPercent.text = "$percent %"
                delay(100L)
            }
            // Finished cycle
            stopVibrationCleaning()
            showSuccess()
        }
    }

    private fun stopVibrationCleaning() {
        isVibrating = false
        progressJob?.cancel()
        progressJob = null

        viewModel.stopCleaning()
        stopHardwareVibrate()

        binding.viewActiveBlueRing.visibility = android.view.View.INVISIBLE
        binding.lottieGlow.visibility = android.view.View.INVISIBLE
        binding.lottieGlow.cancelAnimation()
        binding.btnVibrationStartStop.text = "Start"
        binding.btnVibrationStartStop.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.primary_blue_selected)
        )
        binding.tvVibrationPercent.text = "0 %"

        // Re-enable mode selectors
        binding.cardModeNormal.isEnabled = true
        binding.cardModeStrong.isEnabled = true
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    if (uiState.state is CleaningState.Error) {
                        stopVibrationCleaning()
                        Toast.makeText(this@VibrationCleanActivity, "Lá»—i: " + (uiState.state as CleaningState.Error).message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startHardwareVibrate(strong: Boolean) {
        try {
            android.util.Log.d("VibrateClean", "startHardwareVibrate: strong = $strong")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (!vibrator.hasVibrator()) {
                android.util.Log.w("VibrateClean", "Device does not have a physical vibrator!")
                return
            }

            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .build()

            val pattern = if (strong) {
                longArrayOf(0, 150, 50, 150, 50) // Rapid pulsing
            } else {
                longArrayOf(0, 400, 400) // Slower pulsing
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 1)
                android.util.Log.d("VibrateClean", "Vibrating with VibrationEffect (default amplitude)")
                vibrator.vibrate(effect, audioAttributes)
            } else {
                android.util.Log.d("VibrateClean", "Vibrating with legacy vibrate pattern")
                vibrator.vibrate(pattern, 1)
            }
        } catch (e: Exception) {
            android.util.Log.e("VibrateClean", "Error in startHardwareVibrate", e)
        }
    }

    @Suppress("DEPRECATION")
    private fun stopHardwareVibrate() {
        try {
            android.util.Log.d("VibrateClean", "stopHardwareVibrate called")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.cancel()
        } catch (e: Exception) {
            android.util.Log.e("VibrateClean", "Error in stopHardwareVibrate", e)
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

    private fun showSuccess() {
        val intent = Intent(this, TestSpeakerActivity::class.java)
        startActivity(intent)
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
                    val adView = LayoutInflater.from(this@VibrationCleanActivity)
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
        stopVibrationCleaning()
    }
}

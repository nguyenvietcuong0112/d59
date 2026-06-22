package com.removedust.speaker.cleaner.presentation.ui.airblow

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import androidx.activity.viewModels
import com.removedust.speaker.cleaner.base.BaseActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.databinding.ActivityAirBlowBinding
import com.removedust.speaker.cleaner.presentation.state.CleaningState
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import com.removedust.speaker.cleaner.presentation.ui.main.MainViewModel
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
class AirBlowActivity : BaseActivity() {

    private lateinit var binding: ActivityAirBlowBinding
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    private var selectedSpeed = AirBlowSpeed.MEDIUM
    private var isPlaying = false

    private enum class AirBlowSpeed {
        LOW, MEDIUM, BOOST
    }

    override fun bind() {
        binding = ActivityAirBlowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this


        updateSpeedButtonsUI()
        setupListeners()
        observeViewModel()
        loadAdsNative()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            viewModel.stopCleaning()
            finish()
        }

        binding.btnSpeedDecrease.setOnClickListener {
            val newSpeed = when (selectedSpeed) {
                AirBlowSpeed.BOOST -> AirBlowSpeed.MEDIUM
                AirBlowSpeed.MEDIUM -> AirBlowSpeed.LOW
                AirBlowSpeed.LOW -> AirBlowSpeed.LOW
            }
            if (selectedSpeed != newSpeed) {
                selectedSpeed = newSpeed
                updateSpeedButtonsUI()
                if (isPlaying) {
                    val frequency = when (selectedSpeed) {
                        AirBlowSpeed.LOW -> 70
                        AirBlowSpeed.MEDIUM -> 60
                        AirBlowSpeed.BOOST -> 50
                    }
                    viewModel.updateManualFrequency(frequency)
                    startFanRotation(selectedSpeed)
                }
            }
        }

        binding.btnSpeedIncrease.setOnClickListener {
            val newSpeed = when (selectedSpeed) {
                AirBlowSpeed.LOW -> AirBlowSpeed.MEDIUM
                AirBlowSpeed.MEDIUM -> AirBlowSpeed.BOOST
                AirBlowSpeed.BOOST -> AirBlowSpeed.BOOST
            }
            if (selectedSpeed != newSpeed) {
                selectedSpeed = newSpeed
                updateSpeedButtonsUI()
                if (isPlaying) {
                    val frequency = when (selectedSpeed) {
                        AirBlowSpeed.LOW -> 70
                        AirBlowSpeed.MEDIUM -> 60
                        AirBlowSpeed.BOOST -> 50
                    }
                    viewModel.updateManualFrequency(frequency)
                    startFanRotation(selectedSpeed)
                }
            }
        }

        binding.btnPowerStartStop.setOnClickListener {
            if (isPlaying) {
                viewModel.stopCleaning()
            } else {
                checkVolumeAndRun {
                    AdsConfig.showInterClickAd(this) {
                        val frequency = when (selectedSpeed) {
                            AirBlowSpeed.LOW -> 70
                            AirBlowSpeed.MEDIUM -> 60
                            AirBlowSpeed.BOOST -> 50
                        }
                        viewModel.startManualCleaning(frequency)
                    }
                }
            }
        }
    }

    private fun updateSpeedButtonsUI() {
        binding.btnSpeedDecrease.isEnabled = (selectedSpeed != AirBlowSpeed.LOW)
        binding.btnSpeedDecrease.alpha = if (selectedSpeed == AirBlowSpeed.LOW) 0.5f else 1.0f

        binding.btnSpeedIncrease.isEnabled = (selectedSpeed != AirBlowSpeed.BOOST)
        binding.btnSpeedIncrease.alpha = if (selectedSpeed == AirBlowSpeed.BOOST) 0.5f else 1.0f

        moveArrowToSpeed(selectedSpeed)
    }

    private fun moveArrowToSpeed(speed: AirBlowSpeed) {
        val targetView = when (speed) {
            AirBlowSpeed.LOW -> binding.tvLowLabel
            AirBlowSpeed.MEDIUM -> binding.tvMediumLabel
            AirBlowSpeed.BOOST -> binding.tvHighLabel
        }
        targetView.post {
            val targetCenter = targetView.left + targetView.width / 2f
            val arrowOriginalCenter = binding.ivScaleArrow.left + binding.ivScaleArrow.width / 2f
            val translationX = targetCenter - arrowOriginalCenter
            binding.ivScaleArrow.animate()
                .translationX(translationX)
                .setDuration(250)
                .start()
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
        when (val state = uiState.state) {
            is CleaningState.Idle -> {
                isPlaying = false
                stopFanRotation()
                binding.viewActiveBlueRing.visibility = View.INVISIBLE
                binding.btnPowerStartStop.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue_selected))
                binding.tvStartStop.text = getString(R.string.btn_start)
            }
            is CleaningState.Cleaning -> {
                isPlaying = true
                startFanRotation(selectedSpeed)
                binding.viewActiveBlueRing.visibility = View.VISIBLE
                binding.btnPowerStartStop.setCardBackgroundColor(ContextCompat.getColor(this, R.color.error))
                binding.tvStartStop.text = getString(R.string.btn_stop)
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

    private fun startFanRotation(speed: AirBlowSpeed) {
        val animationSpeed = when (speed) {
            AirBlowSpeed.LOW -> 0.85f
            AirBlowSpeed.MEDIUM -> 1.2f
            AirBlowSpeed.BOOST -> 2f
        }
        binding.ivPropeller.speed = animationSpeed
        if (!binding.ivPropeller.isAnimating) {
            binding.ivPropeller.playAnimation()
        }
    }

    private fun stopFanRotation() {
        if (binding.ivPropeller.isAnimating) {
            binding.ivPropeller.cancelAnimation()
        }
        binding.ivPropeller.progress = 0f
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
                    val adView = LayoutInflater.from(this@AirBlowActivity)
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
        stopFanRotation()
    }
}

package com.removedust.speaker.cleaner.presentation.ui.airblow

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.databinding.ActivityAirBlowBinding
import com.removedust.speaker.cleaner.presentation.state.CleaningState
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import com.removedust.speaker.cleaner.presentation.ui.main.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.removedust.speaker.cleaner.util.showVolumeWarningDialog
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AirBlowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAirBlowBinding
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    private var selectedSpeed = AirBlowSpeed.MEDIUM
    private var fanAnimator: ObjectAnimator? = null
    private var isPlaying = false

    private enum class AirBlowSpeed {
        LOW, MEDIUM, BOOST
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAirBlowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this


        updateSpeedButtonsUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            viewModel.stopCleaning()
            finish()
        }

        binding.btnSpeedDecrease.setOnClickListener {
            if (selectedSpeed != AirBlowSpeed.LOW) {
                selectedSpeed = AirBlowSpeed.LOW
                updateSpeedButtonsUI()
                if (isPlaying) {
                    viewModel.updateManualFrequency(70)
                    startFanRotation(AirBlowSpeed.LOW)
                }
            }
        }

        binding.btnSpeedMedium.setOnClickListener {
            if (selectedSpeed != AirBlowSpeed.MEDIUM) {
                selectedSpeed = AirBlowSpeed.MEDIUM
                updateSpeedButtonsUI()
                if (isPlaying) {
                    viewModel.updateManualFrequency(60)
                    startFanRotation(AirBlowSpeed.MEDIUM)
                }
            }
        }

        binding.btnSpeedIncrease.setOnClickListener {
            if (selectedSpeed != AirBlowSpeed.BOOST) {
                selectedSpeed = AirBlowSpeed.BOOST
                updateSpeedButtonsUI()
                if (isPlaying) {
                    viewModel.updateManualFrequency(50)
                    startFanRotation(AirBlowSpeed.BOOST)
                }
            }
        }

        binding.btnPowerStartStop.setOnClickListener {
            if (isPlaying) {
                viewModel.stopCleaning()
            } else {
                checkVolumeAndRun {
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

    private fun updateSpeedButtonsUI() {
        binding.btnSpeedDecrease.isSelected = (selectedSpeed == AirBlowSpeed.LOW)
        binding.btnSpeedMedium.isSelected = (selectedSpeed == AirBlowSpeed.MEDIUM)
        binding.btnSpeedIncrease.isSelected = (selectedSpeed == AirBlowSpeed.BOOST)
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
                binding.btnPowerStartStop.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue_selected))
                binding.tvStartStop.text = "Start"
            }
            is CleaningState.Cleaning -> {
                isPlaying = true
                startFanRotation(selectedSpeed)
                binding.btnPowerStartStop.setCardBackgroundColor(ContextCompat.getColor(this, R.color.error))
                binding.tvStartStop.text = "Stop"
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
        fanAnimator?.cancel()
        val duration = when (speed) {
            AirBlowSpeed.LOW -> 1000L
            AirBlowSpeed.MEDIUM -> 700L
            AirBlowSpeed.BOOST -> 400L
        }
        fanAnimator = ObjectAnimator.ofFloat(binding.ivPropeller, "rotation", 0f, 360f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            setDuration(duration)
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopFanRotation() {
        fanAnimator?.cancel()
        fanAnimator = null
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopCleaning()
        stopFanRotation()
    }
}

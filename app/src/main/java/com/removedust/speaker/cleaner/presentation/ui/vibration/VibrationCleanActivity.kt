package com.removedust.speaker.cleaner.presentation.ui.vibration

import android.content.Context
import android.content.res.ColorStateList
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VibrationCleanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVibrationCleanBinding
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    private var isStrongMode = false
    private var isVibrating = false
    private var progressJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVibrationCleanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this


        updateSelectorUI()
        setupListeners()
        observeViewModel()
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
                    startVibrationCleaning()
                }
            }
        }
    }

    private fun updateSelectorUI() {
        val selectedStroke = ContextCompat.getColor(this, R.color.primary)
        val unselectedStroke = ContextCompat.getColor(this, R.color.gray_light)

        if (!isStrongMode) {
            binding.cardModeNormal.setStrokeColor(ColorStateList.valueOf(selectedStroke))
            binding.cardModeNormal.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_light)))
            binding.ivModeNormal.imageTintList = ColorStateList.valueOf(selectedStroke)
            binding.tvModeNormal.setTextColor(selectedStroke)

            binding.cardModeStrong.setStrokeColor(ColorStateList.valueOf(unselectedStroke))
            binding.cardModeStrong.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_light)))
            binding.ivModeStrong.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_dark))
            binding.tvModeStrong.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        } else {
            binding.cardModeNormal.setStrokeColor(ColorStateList.valueOf(unselectedStroke))
            binding.cardModeNormal.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_light)))
            binding.ivModeNormal.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_dark))
            binding.tvModeNormal.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))

            binding.cardModeStrong.setStrokeColor(ColorStateList.valueOf(selectedStroke))
            binding.cardModeStrong.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_light)))
            binding.ivModeStrong.imageTintList = ColorStateList.valueOf(selectedStroke)
            binding.tvModeStrong.setTextColor(selectedStroke)
        }
    }

    private fun startVibrationCleaning() {
        isVibrating = true
        binding.btnVibrationStartStop.text = "Dá»«ng"
        binding.btnVibrationStartStop.setBackgroundColor(ContextCompat.getColor(this, R.color.error))

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
            showSuccessDialog()
        }
    }

    private fun stopVibrationCleaning() {
        isVibrating = false
        progressJob?.cancel()
        progressJob = null

        viewModel.stopCleaning()
        stopHardwareVibrate()

        binding.btnVibrationStartStop.text = "Báº¯t Ä‘áº§u"
        binding.btnVibrationStartStop.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
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
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.volume_warning_title)
                .setMessage(R.string.volume_warning_desc)
                .setPositiveButton(R.string.btn_ok) { _, _ -> action() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            action()
        }
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("HoÃ n thÃ nh")
            .setMessage("ÄÃ£ lÃ m sáº¡ch mÃ ng loa báº±ng cÆ¡ cháº¿ rung váº­t lÃ½ thÃ nh cÃ´ng!")
            .setPositiveButton(R.string.btn_ok, null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVibrationCleaning()
    }
}

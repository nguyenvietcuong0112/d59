package com.removedust.speaker.cleaner.presentation.ui.manual

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.databinding.ActivityManualCleanBinding
import com.removedust.speaker.cleaner.presentation.state.CleaningState
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import com.removedust.speaker.cleaner.presentation.ui.main.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManualCleanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManualCleanBinding
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualCleanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this


        setupListeners()
        observeViewModel()
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
                    val freq = binding.frequencySlider.getFrequency()
                    viewModel.startManualCleaning(freq)
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
                binding.tvManualState.text = "Sáºµn sÃ ng"
                binding.manualProgressIndicator.progress = 0
                binding.manualProgressIndicator.isIndeterminate = false
                binding.btnManualStartStop.text = "Báº¯t Ä‘áº§u"
                binding.btnManualStartStop.setIconResource(android.R.drawable.ic_media_play)
                binding.frequencySlider.setEnabled(true)
            }
            is CleaningState.Cleaning -> {
                binding.tvManualState.text = "Äang phÃ¡t Ã¢m thanh"
                binding.tvManualFrequency.text = getString(R.string.text_frequency, uiState.currentFrequency)
                binding.frequencySlider.setFrequency(uiState.currentFrequency)
                binding.manualProgressIndicator.isIndeterminate = true
                binding.btnManualStartStop.text = "Dá»«ng láº¡i"
                binding.btnManualStartStop.setIconResource(android.R.drawable.ic_media_pause)
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopCleaning()
    }
}

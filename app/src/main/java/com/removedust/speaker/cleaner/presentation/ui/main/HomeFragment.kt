package com.removedust.speaker.cleaner.presentation.ui.main

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.base.AbsBaseFragment
import com.removedust.speaker.cleaner.databinding.FragmentHomeBinding
import com.removedust.speaker.cleaner.presentation.state.CleaningState
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import com.removedust.speaker.cleaner.presentation.ui.main.MainViewModel
import com.removedust.speaker.cleaner.presentation.ui.manual.ManualCleanActivity
import com.removedust.speaker.cleaner.presentation.ui.vibration.VibrationCleanActivity
import com.removedust.speaker.cleaner.presentation.ui.airblow.AirBlowActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.removedust.speaker.cleaner.util.showVolumeWarningDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : AbsBaseFragment<FragmentHomeBinding>() {

    private lateinit var viewModel: MainViewModel

    override val layout: Int
        get() = R.layout.fragment_home

    override fun initView() {
        viewModel = ViewModelProvider(requireActivity(), MainViewModel.Factory)[MainViewModel::class.java]
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding?.btnMainAutoClean?.setOnClickListener {
            val state = viewModel.uiState.value.state
            if (state is CleaningState.Cleaning) {
                viewModel.stopCleaning()
            } else {
                checkVolumeAndRun {
                    viewModel.startAutoCleaning()
                }
            }
        }

        binding?.btnStopCleaning?.setOnClickListener {
            viewModel.stopCleaning()
        }

        binding?.cardManualClean?.setOnClickListener {
            val intent = Intent(requireContext(), ManualCleanActivity::class.java)
            startActivity(intent)
        }

        binding?.cardVibrationClean?.setOnClickListener {
            val intent = Intent(requireContext(), VibrationCleanActivity::class.java)
            startActivity(intent)
        }

        binding?.cardAirBlow?.setOnClickListener {
            val intent = Intent(requireContext(), AirBlowActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    updateUI(uiState)
                }
            }
        }
    }

    private fun updateUI(uiState: CleanupUIState) {
        val binding = binding ?: return
        when (val state = uiState.state) {
            is CleaningState.Idle -> {
                binding.tvCurrentState.text = "Ready to clean"
                binding.progressIndicator.progress = 0
                binding.btnMainAutoClean.setImageResource(R.drawable.ic_cleaner_unselected)
                binding.btnMainAutoClean.isEnabled = true

                binding.layoutActiveStatusTop.visibility = View.INVISIBLE
                binding.btnStopCleaning.visibility = View.GONE
                binding.tvTapBanner.visibility = View.VISIBLE

                setCardsEnabled(true)
            }
            is CleaningState.Cleaning -> {
                binding.tvCurrentState.text = "Cleaning Active"
                binding.progressIndicator.progress = uiState.progress
                binding.btnMainAutoClean.setImageResource(R.drawable.ic_cleaner_selected)

                binding.layoutActiveStatusTop.visibility = View.VISIBLE
                binding.btnStopCleaning.visibility = View.VISIBLE
                binding.tvTapBanner.visibility = View.GONE

                binding.tvTimeRemaining.text = "Time Remaining: ${uiState.timeRemaining}s"
                binding.tvLiveFrequency.text = "${uiState.currentFrequency} Hz"

                setCardsEnabled(false)
            }
            is CleaningState.Complete -> {
                viewModel.stopCleaning()
                showSuccessDialog()
            }
            is CleaningState.Error -> {
                viewModel.stopCleaning()
                Toast.makeText(requireContext(), getString(R.string.state_error, state.message), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setCardsEnabled(enabled: Boolean) {
        binding?.cardManualClean?.isEnabled = enabled
        binding?.cardVibrationClean?.isEnabled = enabled
        binding?.cardAirBlow?.isEnabled = enabled
    }

    private fun checkVolumeAndRun(action: () -> Unit) {
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        if (currentVolume < maxVolume) {
            requireContext().showVolumeWarningDialog {
                action()
            }
        } else {
            action()
        }
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.state_complete)
            .setMessage(R.string.success_alert_desc)
            .setPositiveButton(R.string.btn_ok, null)
            .show()
    }
}

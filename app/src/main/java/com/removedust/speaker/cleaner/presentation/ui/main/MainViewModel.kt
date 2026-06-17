package com.removedust.speaker.cleaner.presentation.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.removedust.speaker.cleaner.Application
import com.removedust.speaker.cleaner.domain.usecase.AutoCleaningUseCase
import com.removedust.speaker.cleaner.domain.usecase.HeadphoneCleaningUseCase
import com.removedust.speaker.cleaner.domain.usecase.ManualCleaningUseCase
import com.removedust.speaker.cleaner.domain.usecase.SoundFixUseCase
import com.removedust.speaker.cleaner.domain.usecase.StopCleaningUseCase
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val autoCleaningUseCase: AutoCleaningUseCase,
    private val manualCleaningUseCase: ManualCleaningUseCase,
    private val headphoneCleaningUseCase: HeadphoneCleaningUseCase,
    private val soundFixUseCase: SoundFixUseCase,
    private val stopCleaningUseCase: StopCleaningUseCase
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = Application.Companion.instance
                return MainViewModel(
                    app.autoCleaningUseCase,
                    app.manualCleaningUseCase,
                    app.headphoneCleaningUseCase,
                    app.soundFixUseCase,
                    app.stopCleaningUseCase
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(CleanupUIState())
    val uiState: StateFlow<CleanupUIState> = _uiState.asStateFlow()

    private var cleaningJob: Job? = null

    fun startAutoCleaning() {
        cancelActiveJob()
        cleaningJob = viewModelScope.launch {
            autoCleaningUseCase.execute().collect { state ->
                _uiState.value = state
            }
        }
    }

    fun startManualCleaning(frequency: Int) {
        cancelActiveJob()
        cleaningJob = viewModelScope.launch {
            manualCleaningUseCase.execute(frequency).collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateManualFrequency(frequency: Int) {
        manualCleaningUseCase.updateFrequency(frequency)
    }


    fun stopCleaning() {
        cancelActiveJob()
        cleaningJob = viewModelScope.launch {
            stopCleaningUseCase.execute().collect { state ->
                _uiState.value = state
            }
        }
    }

    fun startHeadphoneCleaning() {
        cancelActiveJob()
        cleaningJob = viewModelScope.launch {
            headphoneCleaningUseCase.execute().collect { state ->
                _uiState.value = state
            }
        }
    }

    fun fixSound() {
        cancelActiveJob()
        cleaningJob = viewModelScope.launch {
            soundFixUseCase.execute().collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun cancelActiveJob() {
        cleaningJob?.cancel()
        cleaningJob = null
    }

    override fun onCleared() {
        super.onCleared()
        // Stop audio track generation on clearance of the ViewModel scope
        stopCleaning()
    }
}
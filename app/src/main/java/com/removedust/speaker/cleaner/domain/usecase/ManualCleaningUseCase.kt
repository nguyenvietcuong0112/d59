package com.removedust.speaker.cleaner.domain.usecase

import com.removedust.speaker.cleaner.domain.repository.SpeakerCleanerRepository
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import kotlinx.coroutines.flow.Flow

class ManualCleaningUseCase(
    private val repository: SpeakerCleanerRepository
) {
    fun execute(frequency: Int): Flow<CleanupUIState> {
        return repository.startManualCleaning(frequency)
    }

    fun updateFrequency(frequency: Int) {
        repository.updateManualFrequency(frequency)
    }
}


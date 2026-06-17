package com.removedust.speaker.cleaner.domain.usecase

import com.removedust.speaker.cleaner.domain.repository.SpeakerCleanerRepository
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import kotlinx.coroutines.flow.Flow

class SoundFixUseCase(
    private val repository: SpeakerCleanerRepository
) {
    fun execute(): Flow<CleanupUIState> {
        return repository.fixSound()
    }
}

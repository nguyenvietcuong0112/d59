package com.removedust.speaker.cleaner.domain.repository

import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import kotlinx.coroutines.flow.Flow

interface SpeakerCleanerRepository {
    fun startAutoCleaning(): Flow<CleanupUIState>
    fun startManualCleaning(frequency: Int): Flow<CleanupUIState>
    fun stopCleaning(): Flow<CleanupUIState>
    fun startHeadphoneCleaning(): Flow<CleanupUIState>
    fun fixSound(): Flow<CleanupUIState>
    fun updateManualFrequency(frequency: Int)
}


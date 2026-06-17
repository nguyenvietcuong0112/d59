package com.removedust.speaker.cleaner.presentation.state

sealed class CleaningState {
    object Idle : CleaningState()
    object Cleaning : CleaningState()
    data class Error(val message: String) : CleaningState()
    data class Complete(val duration: Int) : CleaningState()
}

data class CleanupUIState(
    val state: CleaningState = CleaningState.Idle,
    val progress: Int = 0,
    val currentFrequency: Int = 0,
    val timeRemaining: Int = 0
)

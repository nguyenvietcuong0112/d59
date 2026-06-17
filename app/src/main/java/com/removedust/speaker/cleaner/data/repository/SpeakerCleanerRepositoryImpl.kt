package com.removedust.speaker.cleaner.data.repository

import com.removedust.speaker.cleaner.data.datasource.AudioDatasource
import com.removedust.speaker.cleaner.domain.model.CleaningConfig
import com.removedust.speaker.cleaner.domain.repository.SpeakerCleanerRepository
import com.removedust.speaker.cleaner.presentation.state.CleaningState
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CancellationException

class SpeakerCleanerRepositoryImpl(
    private val audioDatasource: AudioDatasource
) : SpeakerCleanerRepository {

    private val manualFrequency = MutableStateFlow(1000)

    override fun startAutoCleaning(): Flow<CleanupUIState> {
        val config = CleaningConfig(
            durationSeconds = 80,
            startFrequency = 50,
            endFrequency = 10000,
            isSweep = true
        )
        return startCleaningFlow(config)
    }

    override fun startManualCleaning(frequency: Int): Flow<CleanupUIState> = flow {
        manualFrequency.value = frequency
        try {
            manualFrequency.collect { freq ->
                audioDatasource.playFrequency(freq)
                emit(
                    CleanupUIState(
                        state = CleaningState.Cleaning,
                        progress = 0,
                        currentFrequency = freq,
                        timeRemaining = 0
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            audioDatasource.stop()
            emit(CleanupUIState(state = CleaningState.Error(e.message ?: "Manual cleaning error")))
        }
    }.flowOn(Dispatchers.Default)

    override fun updateManualFrequency(frequency: Int) {
        manualFrequency.value = frequency
    }

    override fun stopCleaning(): Flow<CleanupUIState> = flow {
        audioDatasource.stop()
        emit(
            CleanupUIState(
                state = CleaningState.Idle,
                progress = 0,
                currentFrequency = 0,
                timeRemaining = 0
            )
        )
    }.flowOn(Dispatchers.Default)


    override fun startHeadphoneCleaning(): Flow<CleanupUIState> {
        val config = CleaningConfig(
            durationSeconds = 45,
            startFrequency = 100,
            endFrequency = 15000,
            isSweep = true
        )
        return startCleaningFlow(config)
    }

    override fun fixSound(): Flow<CleanupUIState> {
        // Boost audio using test tone at 1000 Hz for 10 seconds or fixed play
        val config = CleaningConfig(
            durationSeconds = 10,
            startFrequency = 1000,
            endFrequency = 1000,
            isSweep = false
        )
        return startCleaningFlow(config)
    }

    private fun startCleaningFlow(config: CleaningConfig): Flow<CleanupUIState> = flow {
        emit(
            CleanupUIState(
                state = CleaningState.Cleaning,
                progress = 0,
                currentFrequency = config.startFrequency,
                timeRemaining = config.durationSeconds
            )
        )

        val totalTicks = config.durationSeconds * 10 // 10 ticks per second (100ms interval)
        val tickIntervalMs = 100L
        val freqRange = (config.endFrequency - config.startFrequency).toFloat()

        try {
            for (tick in 1..totalTicks) {
                if (!currentCoroutineContext().isActive) break
                delay(tickIntervalMs)

                val progress = (tick * 100) / totalTicks
                val currentFreq = if (config.isSweep) {
                    (config.startFrequency + freqRange * (tick.toFloat() / totalTicks)).toInt()
                } else {
                    config.startFrequency
                }

                // Remaining time in seconds
                val timeRemaining = ((totalTicks - tick) + 9) / 10 // Rounded up to the nearest second

                audioDatasource.playFrequency(currentFreq)

                emit(
                    CleanupUIState(
                        state = CleaningState.Cleaning,
                        progress = progress,
                        currentFrequency = currentFreq,
                        timeRemaining = timeRemaining
                    )
                )
            }

            audioDatasource.stop()
            emit(
                CleanupUIState(
                    state = CleaningState.Complete(config.durationSeconds),
                    progress = 100,
                    currentFrequency = 0,
                    timeRemaining = 0
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            audioDatasource.stop()
            emit(
                CleanupUIState(
                    state = CleaningState.Error(e.message ?: "An error occurred during cleaning"),
                    progress = 0,
                    currentFrequency = 0,
                    timeRemaining = 0
                )
            )
        }

    }.flowOn(Dispatchers.Default)
}

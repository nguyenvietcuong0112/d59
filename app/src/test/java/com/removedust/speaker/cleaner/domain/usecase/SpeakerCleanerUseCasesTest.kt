package com.removedust.speaker.cleaner.domain.usecase

import com.removedust.speaker.cleaner.domain.repository.SpeakerCleanerRepository
import com.removedust.speaker.cleaner.presentation.state.CleaningState
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SpeakerCleanerUseCasesTest {

    private lateinit var repository: SpeakerCleanerRepository
    private lateinit var autoUseCase: AutoCleaningUseCase
    private lateinit var manualUseCase: ManualCleaningUseCase
    private lateinit var stopUseCase: StopCleaningUseCase

    @Before
    fun setUp() {
        repository = mockk()
        autoUseCase = AutoCleaningUseCase(repository)
        manualUseCase = ManualCleaningUseCase(repository)
        stopUseCase = StopCleaningUseCase(repository)
    }

    @Test
    fun autoCleaningUseCase_callsRepository() = runBlocking {
        val expectedState = CleanupUIState(CleaningState.Cleaning, 10, 100, 70)
        every { repository.startAutoCleaning() } returns flowOf(expectedState)

        val result = autoUseCase.execute().toList()

        assertEquals(1, result.size)
        assertEquals(expectedState, result[0])
        verify { repository.startAutoCleaning() }
    }

    @Test
    fun manualCleaningUseCase_callsRepository() = runBlocking {
        val freq = 500
        val expectedState = CleanupUIState(CleaningState.Cleaning, 0, freq, 0)
        every { repository.startManualCleaning(freq) } returns flowOf(expectedState)

        val result = manualUseCase.execute(freq).toList()

        assertEquals(1, result.size)
        assertEquals(expectedState, result[0])
        verify { repository.startManualCleaning(freq) }
    }

    @Test
    fun stopCleaningUseCase_callsRepository() = runBlocking {
        val expectedState = CleanupUIState(CleaningState.Idle, 0, 0, 0)
        every { repository.stopCleaning() } returns flowOf(expectedState)

        val result = stopUseCase.execute().toList()

        assertEquals(1, result.size)
        assertEquals(expectedState, result[0])
        verify { repository.stopCleaning() }
    }
}

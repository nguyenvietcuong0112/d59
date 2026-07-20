package com.removedust.speaker.cleaner.presentation.vm

import com.removedust.speaker.cleaner.domain.usecase.AutoCleaningUseCase
import com.removedust.speaker.cleaner.domain.usecase.HeadphoneCleaningUseCase
import com.removedust.speaker.cleaner.domain.usecase.ManualCleaningUseCase
import com.removedust.speaker.cleaner.domain.usecase.SoundFixUseCase
import com.removedust.speaker.cleaner.domain.usecase.StopCleaningUseCase
import com.removedust.speaker.cleaner.presentation.state.CleaningState
import com.removedust.speaker.cleaner.presentation.state.CleanupUIState
import com.removedust.speaker.cleaner.presentation.ui.main.MainViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var autoUseCase: AutoCleaningUseCase
    private lateinit var manualUseCase: ManualCleaningUseCase
    private lateinit var headphoneUseCase: HeadphoneCleaningUseCase
    private lateinit var soundFixUseCase: SoundFixUseCase
    private lateinit var stopUseCase: StopCleaningUseCase
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        autoUseCase = mockk()
        manualUseCase = mockk()
        headphoneUseCase = mockk()
        soundFixUseCase = mockk()
        stopUseCase = mockk()

        viewModel = MainViewModel(
            autoUseCase,
            manualUseCase,
            headphoneUseCase,
            soundFixUseCase,
            stopUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startAutoCleaning_updatesStateFlow() {
        val expectedState = CleanupUIState(CleaningState.Cleaning, 15, 200, 68)
        every { autoUseCase.execute() } returns flowOf(expectedState)

        viewModel.startAutoCleaning()

        assertEquals(expectedState, viewModel.uiState.value)
        verify { autoUseCase.execute() }
    }

    @Test
    fun startManualCleaning_updatesStateFlow() {
        val freq = 440
        val expectedState = CleanupUIState(CleaningState.Cleaning, 0, freq, 0)
        every { manualUseCase.execute(freq) } returns flowOf(expectedState)

        viewModel.startManualCleaning(freq)

        assertEquals(expectedState, viewModel.uiState.value)
        verify { manualUseCase.execute(freq) }
    }

    @Test
    fun stopCleaning_updatesStateFlow() {
        val expectedState = CleanupUIState(CleaningState.Idle, 0, 0, 0)
        every { stopUseCase.execute() } returns flowOf(expectedState)

        viewModel.stopCleaning()

        assertEquals(expectedState, viewModel.uiState.value)
        verify { stopUseCase.execute() }
    }
}

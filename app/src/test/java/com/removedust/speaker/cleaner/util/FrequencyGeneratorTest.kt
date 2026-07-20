package com.removedust.speaker.cleaner.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FrequencyGeneratorTest {

    private val generator = FrequencyGenerator()

    @Test
    fun generateSineWave_createsCorrectSize() {
        val frequency = 1000f
        val durationMs = 1000L // 1 second
        
        val buffer = generator.generateSineWave(frequency, durationMs)
        
        // 44100 samples per second
        assertEquals(44100, buffer.size)
    }

    @Test
    fun generateSineWaveChunk_calculatesPhaseCorrectly() {
        val frequency = 4410f // 10 samples per wave cycle at 44100Hz
        val size = 10
        val startPhase = 0.0
        
        val (buffer, endPhase) = generator.generateSineWaveChunk(frequency, size, startPhase)
        
        assertEquals(10, buffer.size)
        // 1 full cycle, phase should return back to approx 2*PI (which clamps back to 0.0 or slightly below)
        assertTrue(endPhase < 0.1 || Math.abs(endPhase - 2.0 * Math.PI) < 0.1)
    }
}

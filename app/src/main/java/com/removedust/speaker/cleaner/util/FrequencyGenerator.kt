package com.removedust.speaker.cleaner.util

import kotlin.math.sin

class FrequencyGenerator {
    private val SAMPLE_RATE = AudioConstants.SAMPLE_RATE
    private val AMP = AudioConstants.AMP

    /**
     * Generates a sine wave buffer for a specific frequency and duration (in milliseconds).
     */
    fun generateSineWave(frequency: Float, durationMs: Long): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val angleIncrement = 2.0 * Math.PI * frequency / SAMPLE_RATE
        
        for (i in 0 until numSamples) {
            val sample = sin(i * angleIncrement) * AMP
            buffer[i] = sample.toInt().toShort()
        }
        return buffer
    }

    /**
     * Generates a single cycle or small chunk of sine wave for real-time streaming.
     * Keeps track of the phase to prevent clicking sounds during frequency shifts.
     */
    fun generateSineWaveChunk(frequency: Float, size: Int, startPhase: Double): Pair<ShortArray, Double> {
        val buffer = ShortArray(size)
        var phase = startPhase
        val phaseIncrement = 2.0 * Math.PI * frequency / SAMPLE_RATE
        
        for (i in 0 until size) {
            buffer[i] = (sin(phase) * AMP).toInt().toShort()
            phase += phaseIncrement
            if (phase >= 2.0 * Math.PI) {
                phase -= 2.0 * Math.PI
            }
        }
        return Pair(buffer, phase)
    }
}

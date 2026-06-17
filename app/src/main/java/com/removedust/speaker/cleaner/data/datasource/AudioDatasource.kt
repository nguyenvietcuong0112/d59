package com.removedust.speaker.cleaner.data.datasource

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.removedust.speaker.cleaner.util.AudioConstants
import com.removedust.speaker.cleaner.util.FrequencyGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import timber.log.Timber

class AudioDatasource {
    private val generator = FrequencyGenerator()
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val playbackScope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var currentFrequency: Float = 0f

    @Volatile
    private var isPlaying: Boolean = false
    private var phase: Double = 0.0

    private val audioTrackLock = Any()

    fun playFrequency(frequency: Int) {
        currentFrequency = frequency.toFloat()
        if (isPlaying) {
            return
        }

        isPlaying = true

        playbackJob = playbackScope.launch {
            try {
                synchronized(audioTrackLock) {
                    initAudioTrack()
                    audioTrack?.play()
                }
                val chunkSize = 1024

                while (isActive && isPlaying) {
                    val freq = currentFrequency
                    if (freq <= 0f) {
                        val silence = ShortArray(chunkSize)
                        synchronized(audioTrackLock) {
                            if (isPlaying) {
                                audioTrack?.write(silence, 0, chunkSize)
                            }
                        }
                    } else {
                        val (chunk, nextPhase) = generator.generateSineWaveChunk(freq, chunkSize, phase)
                        phase = nextPhase
                        synchronized(audioTrackLock) {
                            if (isPlaying) {
                                audioTrack?.write(chunk, 0, chunkSize)
                            }
                        }
                    }
                    yield()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in audio playback loop")
            } finally {
                synchronized(audioTrackLock) {
                    try {
                        audioTrack?.stop()
                        audioTrack?.flush()
                    } catch (e: Exception) {
                        Timber.e(e, "Error stopping AudioTrack in finally block")
                    }
                }
            }
        }
    }

    private fun initAudioTrack() {
        if (audioTrack == null) {
            val minBufferSize = AudioTrack.getMinBufferSize(
                AudioConstants.SAMPLE_RATE,
                AudioConstants.CHANNEL_CONFIG,
                AudioConstants.AUDIO_FORMAT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setChannelMask(AudioConstants.CHANNEL_CONFIG)
                        .setEncoding(AudioConstants.AUDIO_FORMAT)
                        .setSampleRate(AudioConstants.SAMPLE_RATE)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }
    }

    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        synchronized(audioTrackLock) {
            try {
                audioTrack?.stop()
                audioTrack?.flush()
            } catch (e: Exception) {
                Timber.e(e, "Error stopping AudioTrack in stop()")
            }
            phase = 0.0
        }
    }

    fun release() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        synchronized(audioTrackLock) {
            try {
                audioTrack?.stop()
                audioTrack?.flush()
                audioTrack?.release()
            } catch (e: Exception) {
                Timber.e(e, "Error releasing AudioTrack in release()")
            }
            audioTrack = null
            phase = 0.0
        }
    }
}

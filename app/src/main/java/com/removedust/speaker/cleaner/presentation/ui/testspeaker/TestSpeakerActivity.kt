package com.removedust.speaker.cleaner.presentation.ui.testspeaker

import android.content.Context
import android.content.res.ColorStateList
import android.database.ContentObserver
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.base.BaseActivity
import com.removedust.speaker.cleaner.databinding.ActivityTestSpeakerBinding
import com.removedust.speaker.cleaner.databinding.ActivityVibrationCleanBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestSpeakerActivity : BaseActivity() {

    private lateinit var binding: ActivityTestSpeakerBinding
    private val handler = Handler(Looper.getMainLooper())

    private val volumeRunnable = object : Runnable {
        override fun run() {
            updateSliderFromSystem()
            handler.postDelayed(this, 300)
        }
    }
    private var isPlaying = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager

    override fun bind() {
        binding = ActivityTestSpeakerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        audioManager = getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager

        binding.sliderVolume.trackActiveTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    this,
                    R.color.primary_blue_selected
                )
            )

        binding.sliderVolume.trackInactiveTintList =
            ColorStateList.valueOf(
                Color.parseColor("#E8EDF5")
            )

        initPlayer()
        updateSliderFromSystem()
        handler.post(volumeRunnable)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                stopTest()
            } else {
                startTest()
            }
        }

        binding.sliderVolume.addOnChangeListener { _, value, fromUser ->

            if (!fromUser) return@addOnChangeListener

            setVolume(value.toInt())
        }


    }

    private fun initPlayer() {

        mediaPlayer?.release()

        mediaPlayer = MediaPlayer.create(
            this,
            R.raw.test_speaker
        ).apply {
            isLooping = true
        }
    }

    private fun startTest() {

        mediaPlayer?.start()
        isPlaying = true

        binding.lottieGlow.visibility = View.VISIBLE
        binding.lottieGlow.playAnimation()
    }
    private fun stopTest() {

        mediaPlayer?.pause()
        isPlaying = false

        binding.lottieGlow.cancelAnimation()
        binding.lottieGlow.visibility = View.INVISIBLE
    }
    private fun setVolume(percent: Int) {

        val maxVolume = audioManager.getStreamMaxVolume(
            AudioManager.STREAM_MUSIC
        )

        val volume = (percent * maxVolume) / 100

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume,
            0
        )
    }


    private fun updateSliderFromSystem() {

        val currentVolume =
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        val maxVolume =
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        val percent = currentVolume * 100f / maxVolume

        if (kotlin.math.abs(binding.sliderVolume.value - percent) > 1f) {
            binding.sliderVolume.value = percent
        }
    }

    override fun onResume() {
        super.onResume()
        updateSliderFromSystem()
    }

    override fun onPause() {
        super.onPause()

        mediaPlayer?.pause()

        binding.lottieGlow.cancelAnimation()
    }

    override fun onDestroy() {

        handler.removeCallbacks(volumeRunnable)

        mediaPlayer?.release()
        mediaPlayer = null

        super.onDestroy()
    }
}
package com.removedust.speaker.cleaner.domain.model

data class CleaningConfig(
    val durationSeconds: Int,
    val startFrequency: Int,
    val endFrequency: Int,
    val isSweep: Boolean = true
)

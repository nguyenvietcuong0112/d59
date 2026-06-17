package com.removedust.speaker.cleaner.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.removedust.speaker.cleaner.R
import com.google.android.material.slider.Slider

class FrequencySlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val valueText: TextView
    private val slider: Slider
    private var onFrequencyChangedListener: ((Int) -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_frequency_slider, this, true)

        valueText = findViewById(R.id.tv_slider_frequency)
        slider = findViewById(R.id.slider_frequency)

        slider.valueFrom = 50f
        slider.valueTo = 10000f
        slider.value = 1000f

        updateText(1000)

        slider.addOnChangeListener { _, value, fromUser ->
            val freq = value.toInt()
            updateText(freq)
            if (fromUser) {
                onFrequencyChangedListener?.invoke(freq)
            }
        }
    }

    fun getFrequency(): Int = slider.value.toInt()

    fun setFrequency(frequency: Int) {
        val clamped = frequency.toFloat().coerceIn(slider.valueFrom, slider.valueTo)
        slider.value = clamped
        updateText(clamped.toInt())
    }

    fun setOnFrequencyChangedListener(listener: (Int) -> Unit) {
        this.onFrequencyChangedListener = listener
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        slider.isEnabled = enabled
    }

    private fun updateText(frequency: Int) {
        valueText.text = context.getString(R.string.text_frequency, frequency)
    }
}

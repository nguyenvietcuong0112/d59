package com.removedust.speaker.cleaner.presentation.ui.main

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import com.removedust.speaker.cleaner.R
import com.removedust.speaker.cleaner.base.AbsBaseFragment
import com.removedust.speaker.cleaner.databinding.FragmentTipsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TipsFragment : AbsBaseFragment<FragmentTipsBinding>() {

    override val layout: Int
        get() = R.layout.fragment_tips

    override fun initView() {
        setupListeners()
        setTipsTabActive(isSpeaker = true)
    }

    private fun setupListeners() {
        val binding = binding ?: return

        binding.btnTipsSpeakerContainer.setOnClickListener {
            binding.layoutSpeakerTips.visibility = View.VISIBLE
            binding.layoutMicTips.visibility = View.GONE
            setTipsTabActive(isSpeaker = true)
        }

        binding.btnTipsMicContainer.setOnClickListener {
            binding.layoutSpeakerTips.visibility = View.GONE
            binding.layoutMicTips.visibility = View.VISIBLE
            setTipsTabActive(isSpeaker = false)
        }

        binding.cardTip1.setOnClickListener {
            startDetailActivity()
        }

        binding.cardTip2.setOnClickListener {
            startDetailActivity()
        }

        binding.cardTip3.setOnClickListener {
            startDetailActivity()
        }

        binding.cardTip4.setOnClickListener {
            startDetailActivity()
        }
    }

    private fun startDetailActivity() {
        val intent = Intent(requireContext(), TipDetailActivity::class.java)
        startActivity(intent)
    }

    private fun setTipsTabActive(isSpeaker: Boolean) {
        val binding = binding ?: return
        val context = requireContext()
        if (isSpeaker) {
            binding.btnTipsSpeaker.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.btnTipsSpeakerContainer.setGlassTabState(true)
            binding.btnTipsMic.setTextColor(ContextCompat.getColor(context, R.color.text_language_dark))
            binding.btnTipsMicContainer.setGlassTabState(false)
        } else {
            binding.btnTipsSpeaker.setTextColor(ContextCompat.getColor(context, R.color.text_language_dark))
            binding.btnTipsSpeakerContainer.setGlassTabState(false)
            binding.btnTipsMic.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.btnTipsMicContainer.setGlassTabState(true)
        }
    }
}


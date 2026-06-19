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
            startDetailActivity(
                title = getString(R.string.tip1_title),
                date = "19 Jun, 2026",
                content = getString(R.string.tip1_desc),
                imageResId = R.drawable.img_tip1
            )
        }

        binding.cardTip2.setOnClickListener {
            startDetailActivity(
                title = getString(R.string.tip2_title),
                date = "19 Jun, 2026",
                content = getString(R.string.tip2_desc),
                imageResId = R.drawable.img_tip2
            )
        }

        binding.cardTip3.setOnClickListener {
            startDetailActivity(
                title = getString(R.string.tip3_title),
                date = "19 Jun, 2026",
                content = getString(R.string.tip3_desc),
                imageResId = R.drawable.img_tip3
            )
        }

        binding.cardTip4.setOnClickListener {
            startDetailActivity(
                title = getString(R.string.tip4_title),
                date = "19 Jun, 2026",
                content = getString(R.string.tip4_desc),
                imageResId = R.drawable.img_tip4
            )
        }
    }

    private fun startDetailActivity(title: String, date: String, content: String, imageResId: Int) {
        val intent = Intent(requireContext(), TipDetailActivity::class.java).apply {
            putExtra("extra_title", title)
            putExtra("extra_date", date)
            putExtra("extra_content", content)
            putExtra("extra_image", imageResId)
        }
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


package com.removedust.speaker.cleaner.presentation.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.removedust.speaker.cleaner.databinding.ActivityTipDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TipDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get optional passed extras, defaulting to standard strings if not passed
        val title = intent.getStringExtra("extra_title") ?: "CƯỜNG DEV – HUYỀN THOẠI BUG KHÔNG AI MUỐN GẶP"
        val date = intent.getStringExtra("extra_date") ?: "12 Jun, 2016"
        val content = intent.getStringExtra("extra_content")

        binding.tvTitle.text = title
        binding.tvDate.text = date
        if (content != null) {
            binding.tvContent.text = content
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}

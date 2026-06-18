package com.removedust.speaker.cleaner.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.removedust.speaker.cleaner.databinding.DialogVolumeWarningBinding

fun Context.showVolumeWarningDialog(onConfirm: () -> Unit) {
    val binding = DialogVolumeWarningBinding.inflate(LayoutInflater.from(this))
    val dialog = AlertDialog.Builder(this)
        .setView(binding.root)
        .create()

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    binding.btnCancel.setOnClickListener {
        dialog.dismiss()
    }

    binding.btnConfirm.setOnClickListener {
        dialog.dismiss()
        onConfirm()
    }

    dialog.show()

    val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
    dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
}

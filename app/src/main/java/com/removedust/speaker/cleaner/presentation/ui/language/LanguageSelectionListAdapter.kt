package com.removedust.speaker.cleaner.presentation.ui.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.removedust.speaker.cleaner.R

class LanguageSelectionListAdapter(
    private val onItemClick: (LanguageModel) -> Unit,
    private val isSelectedPredicate: (LanguageModel) -> Boolean
) : ListAdapter<LanguageModel, LanguageSelectionListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card = view.findViewById<View>(R.id.cardSelection)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val ivFlag = view.findViewById<ImageView>(R.id.ivFlag)
        val ivCheck = view.findViewById<ImageView>(R.id.ivCheckIndicator)

        fun bind(item: LanguageModel) {
            tvTitle.text = item.displayName
            ivFlag.setImageResource(item.flagRes)

            val isChecked = isSelectedPredicate(item)

            if (isChecked) {
                card.setBackgroundResource(R.drawable.bg_language_item_selected)
                tvTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                ivCheck.setImageResource(R.drawable.ic_radio_checked)
            } else {
                card.setBackgroundResource(R.drawable.bg_language_item_unselected)
                tvTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_language_dark))
                ivCheck.setImageResource(R.drawable.ic_radio_unchecked)
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<LanguageModel>() {
        override fun areItemsTheSame(oldItem: LanguageModel, newItem: LanguageModel) = oldItem.code == newItem.code
        override fun areContentsTheSame(oldItem: LanguageModel, newItem: LanguageModel) = oldItem == newItem
    }
}

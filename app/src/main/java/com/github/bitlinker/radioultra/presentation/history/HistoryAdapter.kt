package com.github.bitlinker.radioultra.presentation.history

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.databinding.FragmentHistoryListitemBinding
import com.github.bitlinker.radioultra.domain.HistoryItem
import java.text.SimpleDateFormat
import java.util.*

private val DATE_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())

@BindingAdapter("prettyDate")
fun loadImage(view: TextView, date: Date?) {
    view.text = if (date != null) DATE_FORMAT.format(date) else ""
}

// TODO: links
// TODO: cover?
// TODO: date is optional
// TODO: format by other metadata fields - custom binding adapter?
// TODO: safeargs for history communication

class HistoryItemVH(val binding: FragmentHistoryListitemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: HistoryItem) {
        binding.item = item
        // TODO: check if needed
        binding.executePendingBindings()
    }
}

private class HistoryItemDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
    override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean = oldItem == newItem
}

class HistoryAdapter : ListAdapter<HistoryItem, HistoryItemVH>(HistoryItemDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<FragmentHistoryListitemBinding>(layoutInflater, R.layout.fragment_history_listitem, parent, false)
        return HistoryItemVH(binding)
    }

    override
    fun onBindViewHolder(holder: HistoryItemVH, position: Int) {
        holder.bind(getItem(position))
    }
}
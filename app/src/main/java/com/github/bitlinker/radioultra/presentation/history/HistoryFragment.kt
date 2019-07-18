package com.github.bitlinker.radioultra.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.github.bitlinker.radioultra.databinding.FragmentHistoryBinding
import com.github.bitlinker.radioultra.presentation.BackListener
import kotlinx.android.synthetic.main.fragment_history.*
import org.koin.android.viewmodel.ext.android.viewModel
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.databinding.FragmentHistoryListitemBinding
import com.github.bitlinker.radioultra.domain.HistoryItem
import com.google.android.material.snackbar.Snackbar


// TODO: date is optional
// TODO: format by other metadata fields - custom binding adapter?
private class HistoryItemDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
    override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean = oldItem == newItem
}

class HistoryFragment : Fragment(), BackListener {
    class HistoryItemVH(val binding: FragmentHistoryListitemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItem) {
            binding.item = item
            // TODO: check if needed
            binding.executePendingBindings()
        }
    }

    class HistoryAdapter(private val historyFragment: HistoryFragment) : ListAdapter<HistoryItem, HistoryItemVH>(HistoryItemDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemVH {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<FragmentHistoryListitemBinding>(layoutInflater, R.layout.fragment_history_listitem, parent, false)
            binding.lifecycleOwner = historyFragment.viewLifecycleOwner
            binding.model = historyFragment.vm
            return HistoryItemVH(binding)
        }

        override
        fun onBindViewHolder(holder: HistoryItemVH, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private val vm: HistoryViewModel by viewModel()
    private val adapter = HistoryAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentHistoryBinding>(inflater, R.layout.fragment_history, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = vm
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        rvList.adapter = adapter
        rvList.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL))

        vm.items.observe(this, Observer {
            adapter.submitList(it)
        })

        // TODO: common for activity?
        vm.error.observe(this, Observer {
            Snackbar.make(swrList, R.string.error_generic, Snackbar.LENGTH_LONG).show()
        })
    }

    override fun onBackPressed() {
        vm.onBackPressed()
    }
}
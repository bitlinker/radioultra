package com.github.bitlinker.radioultra.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.bitlinker.radioultra.databinding.FragmentHistoryBinding
import com.github.bitlinker.radioultra.presentation.BackListener
import kotlinx.android.synthetic.main.fragment_history.*
import org.koin.android.viewmodel.ext.android.viewModel
import androidx.recyclerview.widget.DividerItemDecoration
import com.github.bitlinker.radioultra.R
import com.google.android.material.snackbar.Snackbar


class HistoryFragment : Fragment(), BackListener {
    private val vm: HistoryViewModel by viewModel()
    private val adapter = HistoryAdapter()

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
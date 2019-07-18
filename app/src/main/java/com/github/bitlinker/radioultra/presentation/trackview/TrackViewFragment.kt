package com.github.bitlinker.radioultra.presentation.trackview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.databinding.FragmentTrackViewBinding
import com.github.bitlinker.radioultra.presentation.BackListener
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TrackViewFragment : Fragment(), BackListener {
    private val args: TrackViewFragmentArgs by navArgs()
    private val vm: TrackViewModel by viewModel { parametersOf(args.item) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentTrackViewBinding>(inflater, R.layout.fragment_track_view, container, false)
        binding.model = vm
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onBackPressed() = vm.onBackPressed()

    // TODO: common class for fragments? No, settings
}
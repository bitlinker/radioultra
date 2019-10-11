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
import com.github.bitlinker.radioultra.presentation.common.applyMenuTint
import kotlinx.android.synthetic.main.fragment_track_view.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.androidx.scope.currentScope
import org.koin.core.parameter.parametersOf

class TrackViewFragment : Fragment(), BackListener {
    private val args: TrackViewFragmentArgs by navArgs()
    val vm: TrackViewModel by currentScope.viewModel(this) { parametersOf(args.item) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentTrackViewBinding>(inflater, R.layout.fragment_track_view, container, false)
        binding.model = vm
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.applyMenuTint(true, true)
    }

    override fun onBackPressed() = vm.onBackPressed()
}
package com.github.bitlinker.radioultra.presentation.player

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.databinding.FragmentPlayerBinding
import com.github.bitlinker.radioultra.presentation.BackListener
import com.github.bitlinker.radioultra.presentation.common.applyMenuTint
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_player.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.androidx.scope.currentScope

@BindingAdapter("animatedButtonState")
fun setPlayButtonState(fab: FloatingActionButton, state: PlayButtonState) {
    val drawable = when (state) {
        PlayButtonState.PLAYING -> fab.context.getDrawable(R.drawable.play_to_stop_anim)
        PlayButtonState.STOPPED -> fab.context.getDrawable(R.drawable.stop_to_play_anim)
        PlayButtonState.BUFFERING -> fab.context.getDrawable(R.drawable.ic_file_download_black_24dp) // TODO: cache it
    }
    if (fab.drawable != drawable) {
        fab.setImageDrawable(drawable)
        if (drawable is AnimatedVectorDrawable) {
            drawable.start()
        }
    }
}

class PlayerFragment : Fragment(), BackListener {
    val vm: PlayerViewModel by currentScope.viewModel(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentPlayerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = vm
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.applyMenuTint(true, false)
    }

    override fun onBackPressed() {
        vm.onBackPressed()
    }
}
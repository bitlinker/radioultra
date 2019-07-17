package com.github.bitlinker.radioultra.presentation.player

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.databinding.FragmentPlayerBinding
import com.github.bitlinker.radioultra.domain.StreamInfo
import com.github.bitlinker.radioultra.presentation.BackListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import org.koin.android.viewmodel.ext.android.viewModel

// TODO: on long click open shops?

// TODO: inject picasso here
@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    val picasso = Picasso.get()

    // Dbg
    picasso.isLoggingEnabled = true
    picasso.setIndicatorsEnabled(true)

    if (url == null) {
        picasso.load(R.drawable.ultralogo).into(view)
    } else {
        picasso
                .load(url)
                .placeholder(R.drawable.ultralogo)
                .error(R.drawable.ultralogo)
                .into(view)
    }
}

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

@BindingAdapter("streamInfoText")
fun setStreamInfoText(tv: TextView, streamInfo: StreamInfo?) {
    if (streamInfo == null || streamInfo == StreamInfo.EMPTY) {
        tv.visibility = View.GONE
    } else {
        tv.visibility = View.VISIBLE
        val sb = StringBuilder()
        if (streamInfo.bitrate != null) {
            sb.append(tv.context.getString(R.string.fragment_player_streaminfo_bitrate, streamInfo.bitrate / 1000))
            sb.append(' ')
        }
        if (streamInfo.sampleRate != null) {
            sb.append(tv.context.getString(R.string.fragment_player_streaminfo_samplerate, streamInfo.sampleRate / 1000F))
            sb.append(' ')
        }
        if (streamInfo.channels != null) {
            when (streamInfo.channels) {
                1 -> sb.append(tv.context.getString(R.string.fragment_player_streaminfo_mono))
                2 -> sb.append(tv.context.getString(R.string.fragment_player_streaminfo_stereo))
                else -> {
                }
            }
            sb.append(' ')
        }
        tv.text = tv.context.getString(R.string.fragment_player_streaminfo_stream, sb.toString())
    }
}

class PlayerFragment : Fragment(), BackListener {
    companion object {
        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }

    val vm: PlayerViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentPlayerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = vm
        return binding.root
    }

    override fun onBackPressed() {
        vm.onBackPressed()
    }
}
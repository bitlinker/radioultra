package com.github.bitlinker.radioultra.presentation.streamselection

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.domain.RadioStream
import com.github.bitlinker.radioultra.presentation.BackListener
import com.github.bitlinker.radioultra.presentation.trackview.TrackViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class StreamSelectionDialogFragment() : DialogFragment(), BackListener {
    private val args: StreamSelectionDialogFragmentArgs by navArgs()
    private val vm: StreamSelectionViewModel by viewModel { parametersOf(args.args) }

    private fun streamToTitle(stream: RadioStream): String {
        return getString(R.string.stream_name, stream.bitrate, stream.id)
    }

    // Lambda is an setSingleChoiceItems() argument
    @Suppress("MoveLambdaOutsideParentheses")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val streams = vm.streams

        val adb = AlertDialog.Builder(context!!)
                .setTitle(R.string.title_stream_selection)
                .setSingleChoiceItems(
                        streams.map { streamToTitle(it) as CharSequence }.toTypedArray(),
                        vm.currentStream,
                        { dialog, which ->
                            vm.onClick(streams[which])
                            dialog.dismiss()
                        }
                )
                .setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.cancel() })
        return adb.create()
    }

    override fun onBackPressed() {
        vm.onBackPressed()
    }
}
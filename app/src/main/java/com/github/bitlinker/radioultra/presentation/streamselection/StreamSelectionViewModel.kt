package com.github.bitlinker.radioultra.presentation.streamselection

import androidx.lifecycle.ViewModel
import com.github.bitlinker.radioultra.business.ui.StreamSelectionViewInteractor
import com.github.bitlinker.radioultra.domain.RadioStream
import io.reactivex.android.schedulers.AndroidSchedulers

class StreamSelectionViewModel(private val navigator: StreamSelectionNavigator,
                               private val interactor: StreamSelectionViewInteractor,
                               private val args: StreamSelectionArgs
) : ViewModel() {
    var streams: List<RadioStream> = emptyList()
        private set
    var currentStream = -1
        private set

    init {
        streams = args.streams
        streams.forEachIndexed { i, stream ->
            if (stream.id == args.selectedStream.id) {
                currentStream = i
                return@forEachIndexed
            }
        }
    }

    fun onClick(radioStream: RadioStream) {
        interactor.setCurStream(radioStream)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun onBackPressed() = navigator.navigateBack()
}
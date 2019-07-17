package com.github.bitlinker.radioultra.presentation.player

import androidx.lifecycle.ViewModel
import com.github.bitlinker.radioultra.business.player.StreamSelectionInteractor
import com.github.bitlinker.radioultra.domain.RadioStream
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator
import io.reactivex.android.schedulers.AndroidSchedulers

// TODO: custom navigator?
class StreamSelectionViewModel(private val navigator: MainNavigator,
                               private val interactor: StreamSelectionInteractor) : ViewModel() {
    var streams: List<RadioStream> = emptyList()
        private set
    var currentStream = -1
        private set

    init {
        // TODO: this will block ui! Find a better solution
        streams = interactor.getStreams().toList().blockingGet()
        var currentStreamObj = interactor.getCurStream().blockingGet()
        streams.forEachIndexed { i, stream ->
            if (stream.id == currentStreamObj.id) {
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
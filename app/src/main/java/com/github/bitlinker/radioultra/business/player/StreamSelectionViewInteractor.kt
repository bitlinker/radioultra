package com.github.bitlinker.radioultra.business.player

import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.domain.RadioStream
import io.reactivex.Completable

class StreamSelectionViewInteractor(private val streamSelectionInteractor: StreamSelectionInteractor,
                                    private val playerInteractor: PlayerInteractor) {
    fun getStreams() = streamSelectionInteractor.getStreams()


//    return streamSelectionInteractor.getCurStream()
//    .flatMapCompletable { play(it) }

    // TODO: no!
    fun setCurStream(stream: RadioStream) =
            streamSelectionInteractor.setCurStream(stream)
                    .andThen(playerInteractor.getState().singleOrError()
                            .flatMapCompletable {
                                if (it.state.isPlaying()) {
                                    playerInteractor.stop()
                                            .andThen(playerInteractor.play(stream))
                                } else Completable.complete()
                            })


    fun getCurStream() = streamSelectionInteractor.getCurStream()

}
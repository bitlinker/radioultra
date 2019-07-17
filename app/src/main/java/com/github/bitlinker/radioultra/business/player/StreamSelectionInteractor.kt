package com.github.bitlinker.radioultra.business.player

import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.data.radiostreams.PredefinedRadioStreamsRepository
import com.github.bitlinker.radioultra.data.radiostreams.RadioMetadataRepository
import com.github.bitlinker.radioultra.data.storage.PersistentStorage
import com.github.bitlinker.radioultra.domain.RadioStream
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class StreamSelectionInteractor(private val radioMetadataRepository: RadioMetadataRepository,
                                private val predefinedRadioStreamsRepository: PredefinedRadioStreamsRepository,
                                private val persistentStorage: PersistentStorage) {

    // TODO: cache here?
    // TODO: combine by bitrate?
    fun getStreams(): Observable<RadioStream> {
        return Observable.concat(
                radioMetadataRepository.getSteams(),
                predefinedRadioStreamsRepository.getStreams()
        )
    }

    fun setCurStream(stream: RadioStream): Completable {
        return persistentStorage.putCurrentStream(stream)
//                .andThen(playerInteractor.getState().singleOrError()
//                        .flatMapCompletable {
//                            if (it.state.isPlaying()) {
//                                playerInteractor.stop()
//                                        .andThen(playerInteractor.play(stream))
//                            } else Completable.complete()
//                        })
    }

    fun getCurStream(): Single<RadioStream> {
        return persistentStorage.getCurrentStream()
                .switchIfEmpty(getDefaultStream())
    }

    private fun getDefaultStream(): Single<RadioStream> {
        // TODO: (first? highest? lowest? bitrate)
        return getStreams().firstOrError()
    }
}
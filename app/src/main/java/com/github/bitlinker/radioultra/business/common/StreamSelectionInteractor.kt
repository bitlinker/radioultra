package com.github.bitlinker.radioultra.business.common

import com.github.bitlinker.radioultra.data.radiostreams.PredefinedRadioStreamsRepository
import com.github.bitlinker.radioultra.data.radiostreams.RadioMetadataRepository
import com.github.bitlinker.radioultra.data.storage.CurrentRadioStreamRepository
import com.github.bitlinker.radioultra.domain.RadioStream
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class StreamSelectionInteractor(private val radioMetadataRepository: RadioMetadataRepository,
                                private val predefinedRadioStreamsRepository: PredefinedRadioStreamsRepository,
                                private val currentRadioStreamRepository: CurrentRadioStreamRepository) {

    // TODO: cache here?
    // TODO: combine by bitrate?
    // TODO: default ios (first? highest? lowest? bitrate)
    fun getStreams(): Observable<RadioStream> {
        return Observable.concat(
                radioMetadataRepository.getSteams(),
                predefinedRadioStreamsRepository.getStreams()
        )
    }

    fun setCurStream(stream: RadioStream): Completable {
        return currentRadioStreamRepository.putCurrentStream(stream)
    }

    fun getCurStreamObservable(): Observable<RadioStream> {
        return currentRadioStreamRepository.getCurrentStreamObservable()
                .flatMapSingle {
                    if (it.isEmpty()) getDefaultStream()
                    else Single.just(it.value)
                }
    }

    private fun getDefaultStream(): Single<RadioStream> {
        return getStreams().firstOrError()
    }
}
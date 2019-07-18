package com.github.bitlinker.radioultra.business.common

import com.github.bitlinker.radioultra.data.player.PlayerWrapper
import com.github.bitlinker.radioultra.data.radiostreams.RadioMetadataRepository
import com.github.bitlinker.radioultra.domain.TrackMetadata
import io.reactivex.Maybe
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit

// TODO: setting for timed update?
private const val STOPPED_METADATA_UPDATE_INTERVAL = 30000L

class MetadataInteractor(private val radioMetadataRepository: RadioMetadataRepository,
                         private val playerWrapper: PlayerWrapper) {

    fun getCurrentTrackMetadata(): Observable<TrackMetadata> {
        // TODO: update metadata when icy cheanges, but at least every STOPPED_METADATA_UPDATE_INTERVAL
        return Observable.merge(
                intervalMetadataUpdate(),
                playerMetadataObservable()
        )
                .flatMapMaybe {
                    radioMetadataRepository.getCurrentTrack()
                            .toMaybe()
                            .doOnError { Timber.e(it, "Failed to get metadata") }
                            .onErrorResumeNext(Maybe.empty())
                }
    }

    private fun intervalMetadataUpdate(): Observable<Long> {
        return Observable.interval(0L, STOPPED_METADATA_UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
                .doOnNext { Timber.d("Interval metadata update: %d", it) }
    }

    private fun playerMetadataObservable(): Observable<Long> {
        return playerWrapper.getStreamMetadata()
                .doOnNext { Timber.d("Player metadata update: %s", it) }
                .map { -1L }
    }
}
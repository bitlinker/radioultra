package com.github.bitlinker.radioultra.business.playerservice

import com.github.bitlinker.radioultra.data.player.ExoPlayerWrapper
import com.github.bitlinker.radioultra.data.radiostreams.RadioMetadataRepository
import com.github.bitlinker.radioultra.data.settings.SettingsRepository
import com.github.bitlinker.radioultra.domain.TrackMetadata
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Provides metadata information combined from player ICY tags & backend web service
 */
class MetadataInteractor(private val radioMetadataRepository: RadioMetadataRepository,
                         private val playerWrapper: ExoPlayerWrapper,
                         private val settingsRepository: SettingsRepository) {

    fun getCurrentTrackMetadataWithCoverSettings(): Observable<TrackMetadata> {
        return Observable.combineLatest(
                getCurrentTrackMetadata(),
                settingsRepository.isDownloadCoverObservable(),
                BiFunction { metadata, isDownloadCover ->
                    if (isDownloadCover) metadata
                    else metadata.copy(coverLink = null)
                }
        )
    }

    /**
     *  update metadata when icy changes, but at least every N seconds from settings
     */
    private fun getCurrentTrackMetadata(): Observable<TrackMetadata> {

        return Observable.merge( // TODO: switchmap here
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
        return settingsRepository.stoppedMetadataUpdateInterval()
                .flatMap { interval ->
                    Observable.interval(0L, interval, TimeUnit.SECONDS)
                            .doOnNext { Timber.d("Interval metadata update: %d", it) }
                }
    }

    private fun playerMetadataObservable(): Observable<Long> {
        return playerWrapper.getStreamMetadata()
                .doOnNext { Timber.d("Player metadata update: %s", it) }
                .map { -1L }
    }
}
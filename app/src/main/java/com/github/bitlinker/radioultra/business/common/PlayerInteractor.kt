package com.github.bitlinker.radioultra.business.common

import com.github.bitlinker.radioultra.business.player.StreamSelectionInteractor
import com.github.bitlinker.radioultra.data.player.PlayerRepository
import com.github.bitlinker.radioultra.data.settings.SettingsRepository
import com.github.bitlinker.radioultra.data.wakelock.WakelockRepository
import com.github.bitlinker.radioultra.domain.RadioStream
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.github.bitlinker.radioultra.presentation.streamselection.StreamSelectionArgs
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction


// TODO: separate playerInteractor & playerFragmentInteractor
class PlayerInteractor(private val streamSelectionInteractor: StreamSelectionInteractor,
                       private val metadataInteractor: MetadataInteractor,
                       private val playerRepository: PlayerRepository,
                       private val wakelockRepository: WakelockRepository,
                       private val settingsRepository: SettingsRepository) {

    // TODO: wakelock with player
    // service run/stop
    // notifcation update with metadata? in service?

    fun play(): Completable {
        // TODO: update useragent if needed
        //settingsRepository.userAgentStringObservable()
        return getCurrentStream()
                .flatMapCompletable { play(it) }
    }

    fun play(stream: RadioStream): Completable {
        return playerRepository.play(stream)
        //wakelockRepository.aquirePlayerLock()
    }

    fun stop(): Completable {
        return playerRepository.stop()
    }

    fun getState() = playerRepository.getPlayerStatus()

    fun getCurrentStreamInfo() = playerRepository.getStreamInfo()

    private fun getCurrentStream() = streamSelectionInteractor.getCurStream()

    fun getStreamSelectionArgs(): Single<StreamSelectionArgs> {
        return Single.zip(
                streamSelectionInteractor.getStreams().toList(),
                getCurrentStream(),
                BiFunction { list, current -> StreamSelectionArgs(list, current) }
        )
    }

    fun getMetadata(): Observable<TrackMetadata> {
        return Observable.combineLatest(
                metadataInteractor.getCurrentTrackMetadata(),
                settingsRepository.isDownloadCoverObservable(),
                BiFunction { metadata, isDownloadCover ->
                    if (isDownloadCover) metadata
                    else metadata.copy(coverLink = null)
                }
        )
    }
}
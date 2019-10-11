package com.github.bitlinker.radioultra.business.ui

import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.business.common.StreamSelectionInteractor
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.github.bitlinker.radioultra.presentation.streamselection.StreamSelectionArgs
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class PlayerViewInteractor(private val streamSelectionInteractor: StreamSelectionInteractor,
                           private val playerInteractor: PlayerInteractor) {

    fun togglePlayStop(): Completable {
        return playerInteractor.togglePlayStop()
    }

    fun getState() = playerInteractor.getPlayerStatus()

    fun getCurrentStreamInfo() = playerInteractor.getStreamInfo()

    private fun getCurrentStream() = streamSelectionInteractor.getCurStreamObservable().firstOrError()

    fun getStreamSelectionArgs(): Single<StreamSelectionArgs> {
        return Single.zip(
                streamSelectionInteractor.getStreams().toList(),
                getCurrentStream(),
                BiFunction { list, current -> StreamSelectionArgs(list, current) }
        )
    }

    fun getTrackMetadata(): Observable<TrackMetadata> {
        return playerInteractor.getTrackMetadata()
    }
}
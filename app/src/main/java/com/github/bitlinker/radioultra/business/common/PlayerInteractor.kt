package com.github.bitlinker.radioultra.business.common

import android.support.v4.media.session.MediaSessionCompat
import com.github.bitlinker.radioultra.business.playerservice.PlayerServiceApi
import com.github.bitlinker.radioultra.data.playerservice.PlayerServiceConnector
import com.github.bitlinker.radioultra.domain.PlayerStatus
import com.github.bitlinker.radioultra.domain.StreamInfo
import com.github.bitlinker.radioultra.domain.TrackMetadata
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Works with remote player service & repository
 */
class PlayerInteractor(
        private val connector: PlayerServiceConnector
) : PlayerServiceApi {

    fun bind(): Completable {
        return connector.bind()
    }

    override fun getMediaSessionToken(): Single<MediaSessionCompat.Token> {
        return connector.getSingleConnection()
                .flatMap { it.getMediaSessionToken() }
    }

    override fun getTrackMetadata(): Observable<TrackMetadata> {
        return connector.getConnection()
                .switchMap { it.getTrackMetadata() }
    }

    override fun play(): Completable {
        return connector.getSingleConnection()
                .flatMapCompletable { it.play() }
    }

    override fun stop(): Completable {
        return connector.getSingleConnection()
                .flatMapCompletable { it.stop() }
    }

    override fun togglePlayStop(): Completable {
        return connector.getSingleConnection()
                .flatMapCompletable { it.togglePlayStop() }
    }

    override fun getPlayerStatus(): Observable<PlayerStatus> {
        return connector.getConnection()
                .switchMap { it.getPlayerStatus() }
    }

    override fun getStreamInfo(): Observable<StreamInfo> {
        return connector.getConnection()
                .switchMap { it.getStreamInfo() }
    }
}
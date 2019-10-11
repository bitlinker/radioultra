package com.github.bitlinker.radioultra.business.playerservice

import android.support.v4.media.session.MediaSessionCompat
import com.github.bitlinker.radioultra.domain.PlayerStatus
import com.github.bitlinker.radioultra.domain.StreamInfo
import com.github.bitlinker.radioultra.domain.StreamMetadata
import com.github.bitlinker.radioultra.domain.TrackMetadata
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface PlayerServiceApi {
    /**
     * Returns MediaSession token for MediaBrowserService & MediaController
     */
    fun getMediaSessionToken(): Single<MediaSessionCompat.Token>

    fun getPlayerStatus(): Observable<PlayerStatus>

    fun getStreamInfo(): Observable<StreamInfo>

    fun getTrackMetadata(): Observable<TrackMetadata>

    fun play(): Completable

    fun stop(): Completable

    fun togglePlayStop(): Completable
}
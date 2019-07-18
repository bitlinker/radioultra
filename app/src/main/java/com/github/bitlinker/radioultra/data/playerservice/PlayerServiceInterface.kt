package com.github.bitlinker.radioultra.data.playerservice

import com.github.bitlinker.radioultra.domain.PlayerStatus
import com.github.bitlinker.radioultra.domain.TrackMetadata
import io.reactivex.Observable

interface PlayerServiceInterface {
    fun play()

    fun stop()

    fun pause()

    fun playerStatus(): Observable<PlayerStatus>

    fun metadata(): Observable<TrackMetadata>
}
package com.github.bitlinker.radioultra.data.playerservice

import android.content.Context
import android.content.Intent
import com.github.bitlinker.radioultra.business.playerservice.PlayerServiceApi
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

/**
 * This class is used to connect & interact with the remote player service from UI
 */
class PlayerServiceConnector(private val context: Context) {
    private data class PlayerServiceConnection(val binder: PlayerService.LocalBinder?)

    private val connection = BehaviorSubject.create<PlayerServiceConnection>()

    fun getConnection(): Observable<PlayerServiceApi> {
        return connection
                .filter { it.binder != null }
                .map { it.binder!!.getServiceApi() }
    }

    fun getSingleConnection() : Single<PlayerServiceApi> {
        return getConnection()
                .firstOrError()
    }

    /**
     * Binds to the player service. Call this with UI lifecycle
     */
    fun bind(): Completable {
        val intent = Intent(context, PlayerService::class.java)
        return context.bindServiceObservable<PlayerService.LocalBinder>(intent, Context.BIND_AUTO_CREATE)
                .doOnSubscribe { Timber.d("Starting connection") }
                .doOnNext { Timber.d("Connected") }
                .doFinally { Timber.d("Disconnected") }
                .map { connection.onNext(PlayerServiceConnection(it)) }
                .doFinally { connection.onNext(PlayerServiceConnection(null)) }
                .repeat() // Repeat connection if service died
                .ignoreElements()
    }
}
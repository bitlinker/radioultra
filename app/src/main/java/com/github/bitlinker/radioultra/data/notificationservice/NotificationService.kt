package com.github.bitlinker.radioultra.data.notificationservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.github.bitlinker.radioultra.business.notification.NotificationServiceInteractor
import com.github.bitlinker.radioultra.data.player.PlayerRepository
import com.github.bitlinker.radioultra.presentation.notification.NotificationPresenter
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * This service is used to keep application alive during playback & mantain notification
 * Notification control intents are also handled here
 *
 * acts like viewmodel here...
 */
class NotificationService : Service() {
    private val disposable = CompositeDisposable()
    private val interactor: NotificationServiceInteractor by inject()


    // TODO: use interactor here
    // TODO: stop service when no ui & notification destroyed? or on cancel?

    // Stop with notification cancellation event
    // Or when exiting from ui with not playing state

    override fun onBind(intent: Intent?): IBinder? {
        return null
        // TODO: local binder?
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate");
        interactor.onCreate(this)
    }

    override fun onDestroy() {
        Timber.d("onDestroy");
        // TODO: in interactor?
        interactor.onDestroy(this)
        disposable.dispose()
        super.onDestroy()
    }
}
package com.github.bitlinker.radioultra.business.notification

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.data.playerservice.PlayerService
import com.github.bitlinker.radioultra.data.wrappers.*
import com.github.bitlinker.radioultra.presentation.notification.NotificationPresenter
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.lang.IllegalStateException

// TODO: look at ump, it has a lot of fun stuff!
// It may be a good idea to build notification and UI around mediasession info for compatibility?

private const val SINGLE_MEDIA_ID = "SINGLE_MEDIA_ID"

class PlayerServiceController(
        private val playerService: PlayerService, // TODO: inject for notification to work
        private val context: Context, // TODO: named context as service
        private val mediaSessionWrapper: MediaSessionWrapper,
        private val audioFocusWrapper: AudioFocusWrapper,
        private val uiScheduler: Scheduler,
        private val wakelockWrapper: WakelockWrapper,
        private val notificationPresenter: NotificationPresenter,
        private val playerInteractor: PlayerInteractor) {

    private val disposable = CompositeDisposable()
    private val intent = Intent(context, PlayerService::class.java)


    // Player hierarchy:
    // - Player repo (player scope)
    // - HostingRadio repo
    // - Current stream repo
    // - Settings repo

    // - Player interactor: (player scope)
    // -- play / choose and save stream
    // -- updateBufferSizeCompletable
    // -- updateUserAgentCompletable
    // -- composite metadata observable (take toggle flag and cover from setting observable) - separate interactor?
    // -- leave simple metadata for notifications?

    // - PlayConditions interactor:
    // -- handles start/stop audiofocus & wakelock & noisy logic (Completable?)

    // - History interactor uses HostingRadio repo

    // - MediaSession interactor: (player scope)
    // -- updates mediasession according to player state observable

    // - Notification interactor:
    // -- updates notification according to player state (or mediasession state?)
    // -- starts/stops with player service

    // --MainActivity interactor:
    // -- handles play on startup
    // -- binds to service

    // - Player interactor is player scoped, so some binding is required for UI! get interactor? interface?

    init {
        notificationPresenter.startForeground(playerService, mediaSessionWrapper.getToken())

        // TODO: move out business logic as far as possible
        // TODO: work with the player interactor or repo here???
        disposable.add(playerInteractor.getMetadata()
                .subscribe { mediaSessionWrapper.setMetadata(it) } // TODO: handle error
        )

        // TODO: object or function?
        disposable.add(
                wakelockWrapper.aquirePlayerLock(context.packageName + "_playerWakeLock")
                        .subscribe() // TODO: handle error
        )

        disposable.add(
                noisyBroadcastReceiverObservable(context, uiScheduler)
                        .subscribe()  // TODO: handle error, handle value
        )


        // TODO: update locks & notification
//        disposable.add(
//                playerInteractor.getState()
//                        // TODO: scheduler?
//                        .flatMap { t -> t.state.isPlaying() }
//                        .subscribe()
//        )

        //mediaSessionWrapper.setMetadata();
        //mediaSessionWrapper.setPlaybackState()
    }

    // TODO: separate by internal & external calls
    fun start() {
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop() {
        context.stopService(intent)
    }

    fun getMediaId() = SINGLE_MEDIA_ID

    fun isMedia(id: String): Boolean {
        return id == getMediaId()
    }

    fun dispose() {
        disposable.dispose()
        if (!audioFocusWrapper.release()) {
            Timber.e("Failed to release audiofocus")
            // TODO: debug
            throw IllegalStateException("Failed to release audiofocus")
        }
        //notificationPresenter.stopForeground(playerService)
        mediaSessionWrapper.close()
    }

    // To provide token to service...
    fun getMediaSessionRepo() = mediaSessionWrapper
}
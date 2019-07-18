package com.github.bitlinker.radioultra.business.notification

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.data.wrappers.AudioFocusWrapper
import com.github.bitlinker.radioultra.data.wrappers.MediaSessionWrapper
import com.github.bitlinker.radioultra.data.playerservice.PlayerService
import com.github.bitlinker.radioultra.data.wrappers.NoisyBroadcastWrapper
import com.github.bitlinker.radioultra.data.wrappers.WakelockWrapper
import com.github.bitlinker.radioultra.presentation.notification.NotificationPresenter
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
        private val wakelockWrapper: WakelockWrapper,
        private val noisyBroadcastWrapper: NoisyBroadcastWrapper,
        private val notificationPresenter: NotificationPresenter,
        private val playerInteractor: PlayerInteractor) {

    private val disposable = CompositeDisposable()
    private val intent = Intent(context, PlayerService::class.java)

    init {
        notificationPresenter.startForeground(playerService, mediaSessionWrapper.getToken())

        // TODO: work with the player interactor or repo here???
        disposable.add(playerInteractor.getMetadata()
                .subscribe { mediaSessionWrapper.setMetadata(it) } // TODO: handle error
        )

        disposable.add(
                wakelockWrapper.aquirePlayerLock(context.packageName + "_playerWakeLock")
                        .subscribe() // TODO: handle error
        )

        disposable.add(
                noisyBroadcastWrapper.noisyBroadcastObservable()
                        .subscribe()  // TODO: handle error, handle value
        )

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
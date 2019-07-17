package com.github.bitlinker.radioultra.business.notification

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.data.mediasession.MediaSessionRepository
import com.github.bitlinker.radioultra.data.notificationservice.NotificationService
import com.github.bitlinker.radioultra.presentation.notification.NotificationPresenter

// TODO: look at ump, it has a lot of fun stuff!
// It may be a good idea to build notification and UI around mediasession info for compatibility?

class NotificationServiceInteractor(
        private val context: Context,
        private val mediaSessionRepository: MediaSessionRepository,
        private val notificationPresenter: NotificationPresenter,
        private val playerInteractor: PlayerInteractor) {

    private val intent = Intent(context, NotificationService::class.java)

    // TODO: separate by internal & external calls
    fun start() {
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop() {
        context.stopService(intent)
    }

    fun onCreate(notificationService: NotificationService) {
        notificationPresenter.startForeground(notificationService, mediaSessionRepository.getToken())

        // TODO: destory; move this code to some (view)model
        playerInteractor.getMetadata()
                .subscribe { mediaSessionRepository.setMetadata(it) }


        // TODO
        //mediaSessionRepository.setMetadata();
        //mediaSessionRepository.setPlaybackState()
    }

    fun onDestroy(notificationService: NotificationService) {
        notificationPresenter.stopForeground(notificationService)
        mediaSessionRepository.close()
    }
}
package com.github.bitlinker.radioultra.business.playerservice

import android.support.v4.media.session.MediaSessionCompat
import com.github.bitlinker.radioultra.BuildConfig
import com.github.bitlinker.radioultra.business.common.StreamSelectionInteractor
import com.github.bitlinker.radioultra.data.player.ExoPlayerWrapper
import com.github.bitlinker.radioultra.data.playerservice.PlayerService
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.data.settings.SettingsRepository
import com.github.bitlinker.radioultra.data.wrappers.AudioFocusWrapper
import com.github.bitlinker.radioultra.data.wrappers.MediaSessionWrapper
import com.github.bitlinker.radioultra.data.wrappers.NoisyBroadcastWrapper
import com.github.bitlinker.radioultra.data.wrappers.WakelockWrapper
import com.github.bitlinker.radioultra.domain.PlayerStatus
import com.github.bitlinker.radioultra.domain.StreamInfo
import com.github.bitlinker.radioultra.domain.StreamMetadata
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.github.bitlinker.radioultra.presentation.notification.NotificationPresenter
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber

private const val WAKELOCK_TAG = BuildConfig.APPLICATION_ID + "_player_wakelock"

/**
 * Interactor implementing all service business logic
 * Dependencies should be recreated with service to handle cleanup
 */
class PlayerServiceInteractor(
        private val playerWrapper: ExoPlayerWrapper,
        private val mediaSessionWrapper: MediaSessionWrapper,
        private val wakelockWrapper: WakelockWrapper,
        private val audioFocusWrapper: AudioFocusWrapper,
        private val noisyBroadcastWrapper: NoisyBroadcastWrapper,
        private val settingsRepository: SettingsRepository,
        private val notificationPresenter: NotificationPresenter,
        private val metadataInteractor: MetadataInteractor,
        private val streamSelectionInteractor: StreamSelectionInteractor,
        private val schedulerProvider: SchedulerProvider
) : PlayerServiceApi {

    init {
        mediaSessionWrapper.controllerCallback = object : MediaSessionWrapper.ControllerCallback {
            override fun play() {
                this@PlayerServiceInteractor.play()
                        .doOnError(Timber::e)
                        .onErrorComplete()
                        .subscribe()
            }

            override fun stop() {
                this@PlayerServiceInteractor.stop()
                        .doOnError(Timber::e)
                        .onErrorComplete()
                        .subscribe()
            }

        }
    }

    override fun getTrackMetadata(): Observable<TrackMetadata> {
        return metadataInteractor.getCurrentTrackMetadataWithCoverSettings()
    }

    override fun play(): Completable {
        return streamSelectionInteractor.getCurStreamObservable()
                .firstOrError()
                .flatMapCompletable { playerWrapper.play(it) }
    }

    override fun stop(): Completable {
        return playerWrapper.stop()
    }

    override fun togglePlayStop(): Completable {
        return playerWrapper.getPlayerStatus()
                .firstOrError()
                .map { it.state.isPlaying() }
                .flatMapCompletable {
                    if (it) stop()
                    else play()
                }
    }

    override fun getPlayerStatus(): Observable<PlayerStatus> {
        return playerWrapper.getPlayerStatus()
    }

    override fun getStreamInfo(): Observable<StreamInfo> {
        return playerWrapper.getStreamInfo()
    }

    override fun getMediaSessionToken(): Single<MediaSessionCompat.Token> {
        return Single.just(mediaSessionWrapper.getToken())
    }

    fun getMediaSessionNow(): MediaSessionCompat {
        return mediaSessionWrapper.getSession()
    }

    /**
     * Called when service is started and disposed when it is destroyed
     * Should not never complete or throw
     * Closes all dependencies on disposal, so the scope should be recreated to reuse
     */
    fun bindToService(service: PlayerService): Completable {
        // TODO: wrap errrors to prevent error/complete
        val whenPlayingCompletable = playerWrapper.getPlayerStatus()
                .map { it.state.isPlaying() }
                .distinctUntilChanged()
                .switchMapCompletable {
                    if (it) {
                        doWhenBoundAndPlaying(service)
                    } else Completable.never()
                }


        return Completable.merge(
                listOf(
                        doWhenBound(service),
                        whenPlayingCompletable
                )
        )
                .doFinally { close() }
    }

    private fun doWhenBound(service: PlayerService): Completable {
        return Completable.merge(
                listOf(
                        bindMediaSessionAndNotificationPlaybackStateUpdate(),
                        bindUpdateBufferSizeFromSettings(),
                        bindUpdateUserAgentStringFromSettings()
                )
        )
    }

    private fun doWhenBoundAndPlaying(service: PlayerService): Completable {
        return Completable.merge(
                listOf(
                        bindServiceStartStopToPlaybackState(service),
                        bindShowHideForegroundNotification(service),
                        bindSetMediaSessionActiveStatus(),
                        bindMediaSessionAndNotificationMetadataUpdate(),
                        bindWakelock(),
                        bindAudiofocusToPausePlayback(),
                        bindNoisyBroadcastToPausePlayback(),
                        bindUpdateRadioStream()
                )
        )
    }

    /**
     * Sets the MediaSession status to active during subscription
     */
    private fun bindSetMediaSessionActiveStatus(): Completable {
        return Completable.defer {
            mediaSessionWrapper.setActive(true)
            return@defer Completable.never()
        }.doFinally { mediaSessionWrapper.setActive(false) }
                .subscribeOn(schedulerProvider.ui())
    }

    /**
     * Updates metadata in MediaSession and Notification when changed
     */
    private fun bindMediaSessionAndNotificationMetadataUpdate(): Completable {
        return metadataInteractor.getCurrentTrackMetadataWithCoverSettings()
                .flatMap { mediaSessionWrapper.setMetadata(it).andThen(Observable.just(it)) }
                .observeOn(schedulerProvider.ui())
                .map { notificationPresenter.updateNotification() }
                .ignoreElements()
    }

    /**
     * Updates playback state in MediaSession and Notification when changed
     */
    private fun bindMediaSessionAndNotificationPlaybackStateUpdate(): Completable {
        return playerWrapper.getPlayerStatus()
                .observeOn(schedulerProvider.ui())
                .map { mediaSessionWrapper.setPlaybackState(it) }
                .map { notificationPresenter.updateNotification() }
                .ignoreElements()
    }

    /**
     * Acquires wakelock during subscription
     */
    private fun bindWakelock(): Completable {
        return wakelockWrapper.aquirePlayerLock(WAKELOCK_TAG)
    }

    /**
     * Acquires audiofocus and affects playback state if changed
     */
    private fun bindAudiofocusToPausePlayback(): Completable {
        return audioFocusWrapper.request()
                .filter {
                    it == AudioFocusWrapper.State.LOSS || it == AudioFocusWrapper.State.LOSS_TRANSIENT || it == AudioFocusWrapper.State.LOSS_TRANSIENT_CAN_DUCK
                }
                .flatMapCompletable { stop() }
    }

    /**
     * Listens to noisy broadcast and affects playback if needed
     */
    private fun bindNoisyBroadcastToPausePlayback(): Completable {
        return noisyBroadcastWrapper.noisyBroadcastReceiverObservable()
                .flatMapCompletable { playerWrapper.stop() }
    }

    /**
     * Starts foreground notification when playback is started, and disables foreground when stopped,
     * while keeping the notification
     */
    private fun bindShowHideForegroundNotification(service: PlayerService): Completable {
        return Completable.defer {
            notificationPresenter.startForeground(service)
            return@defer Completable.never()
        }
                .doFinally { notificationPresenter.stopForeground(service, false) }
                .subscribeOn(schedulerProvider.ui())
    }

    /**
     * Updates player buffer size when changed
     */
    private fun bindUpdateBufferSizeFromSettings(): Completable {
        return settingsRepository.bufferingTime()
                .flatMapCompletable { playerWrapper.setBufferTime(it) }
    }

    /**
     * Updates player user-agent string when changed
     */
    private fun bindUpdateUserAgentStringFromSettings(): Completable {
        return settingsRepository.userAgentStringObservable()
                .flatMapCompletable { playerWrapper.setUserAgentString(it) }
    }

    /**
     * Updates currently played stream when changed in repo
     */
    private fun bindUpdateRadioStream(): Completable {
        return streamSelectionInteractor.getCurStreamObservable()
                .flatMapCompletable { playerWrapper.play(it) }
    }

    /**
     * Starts and stops service when playback state is changed
     */
    private fun bindServiceStartStopToPlaybackState(service: PlayerService): Completable {
        return Completable.defer {
            service.startMe()
            Completable.never()
        }
                .doFinally { service.stopMe() }
                .subscribeOn(schedulerProvider.ui())
    }

    private fun close() {
        playerWrapper.close()
        mediaSessionWrapper.close()
    }
}

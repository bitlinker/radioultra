package com.github.bitlinker.radioultra.business.ui

import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.data.settings.SettingsRepository
import io.reactivex.Completable
import io.reactivex.internal.operators.completable.CompletableAmb
import timber.log.Timber

class UiStartupInteractor(private val playerInteractor: PlayerInteractor,
                          private val settingsRepository: SettingsRepository) {
    /**
     *
     */
    fun bindToUi(): Completable {
        return Completable.merge(
                listOf(
                        startPlaybackOnUiStartupIfNeeded(),
                        bindPlayerService()
                )
        )
                .doOnSubscribe { Timber.d("UI binding") }
                .doFinally { Timber.d("UI unbinding") }
    }

    /**
     * Binds the player service to UI lifecycle
     */
    private fun bindPlayerService(): Completable {
        return playerInteractor.bind()
    }

    /**
     * Starts playback on UI startup if has corresponding setting
     */
    private fun startPlaybackOnUiStartupIfNeeded(): Completable {
        return settingsRepository.playOnStartupObservable()
                .firstOrError()
                .filter { it }
                .flatMapCompletable {
                    Timber.d("Starting playback on startup")
                    playerInteractor.play()
                }
                .andThen(Completable.never())
    }
}
package com.github.bitlinker.radioultra.business.common

import com.github.bitlinker.radioultra.data.settings.SettingsRepository
import io.reactivex.Completable
import timber.log.Timber

// TODO: wrong name? in player interactor? rename player interactor to player fragment interactor
class StartupInteractor(private val playerInteractor: PlayerInteractor,
                        private val settingsRepository: SettingsRepository) {
    // TODO: use?
    fun onStartup() = Completable.complete()
//    fun onStartup(): Completable {
//        return settingsRepository.playOnStartupObservable()
//                .firstOrError()
//                .filter { it }
//                .flatMapCompletable {
//                    Timber.d("Starting playback on startup")
//                    playerInteractor.play()
//                }
//    }
}
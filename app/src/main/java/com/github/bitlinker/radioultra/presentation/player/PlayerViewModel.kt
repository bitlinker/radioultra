package com.github.bitlinker.radioultra.presentation.player

import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.business.common.StartupInteractor
import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.domain.PlayerStatus
import com.github.bitlinker.radioultra.domain.StreamInfo
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.github.bitlinker.radioultra.presentation.streamselection.StreamSelectionArgs
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

// TODO: how to inflate models with different args?

class PlayerViewModel(private val navigator: PlayerNavigator,
                      private val interactor: PlayerInteractor,
                      private val startupInteractor: StartupInteractor,
                      private val uiScheduler: Scheduler) : ViewModel() {
    private val disposable = CompositeDisposable()

    private val metadata = MutableLiveData<TrackMetadata>()

    val title = Transformations.map(metadata) { it.title } as LiveData<String?>
    val artist = Transformations.map(metadata) { it.artist } as LiveData<String?>
    val coverUrl = Transformations.map(metadata) { it.coverLink } as LiveData<String?>

    val playButtonState = MutableLiveData<PlayButtonState>()
    val streamInfo = MutableLiveData<StreamInfo>()

    init {
        playButtonState.value = PlayButtonState.STOPPED

        disposable.add(
                interactor.getMetadata()
                        .observeOn(uiScheduler)
                        .subscribe(
                                {
                                    Timber.d("Metadata: ${it.toString()}")
                                    metadata.value = it
                                },
                                { showError(it) }
                        )
        )

        disposable.add(startupInteractor.onStartup()
                .observeOn(uiScheduler)
                .subscribe(
                        {},
                        { showError(it) }
                )
        )

        disposable.add(interactor.getState()
                .observeOn(uiScheduler)
                .subscribe(
                        {
                            playButtonState.value = when (it.state) {
                                PlayerStatus.State.STOPPED -> PlayButtonState.STOPPED
                                PlayerStatus.State.STOPPED_ERROR -> {
                                    showError(it.throwable)
                                    PlayButtonState.STOPPED
                                }
                                PlayerStatus.State.BUFFERING -> PlayButtonState.BUFFERING
                                PlayerStatus.State.PLAYING -> PlayButtonState.PLAYING
                            }
                        },
                        { showError(it) }
                )
        )

        disposable.add(interactor.getCurrentStreamInfo()
                .observeOn(uiScheduler)
                .subscribe(
                        { streamInfo.value = it },
                        { showError(it) }
                )
        )
    }

    // TODO: databinding hates nulls!

    // TODO: custom error types
    fun showError(error: Throwable?) {
        Timber.e(error)
        // TODO: show error (activity interface)
    }

    fun onHistoryClicked() = navigator.navigateToHistory()

    fun onChooseStreamClicked() {
        disposable.add(
                interactor.getStreamSelectionArgs()
                        .subscribe(
                                { navigator.showChooseStreamDialog(it) },
                                { showError(it) }
                        )
        )
    }

    fun onMenuItemClicked(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_settings) {
            navigator.navigateToSettings()
            return true
        }
        return false
    }

    fun onCoverClicked() {
        val value = metadata.value
        if (value != null) navigator.navigateToTrackInfo(value)
    }

    fun onPlayStopClicked() {
        val newState: PlayButtonState
        if (playButtonState.value == PlayButtonState.STOPPED) {
            newState = PlayButtonState.PLAYING
            disposable.add(interactor.play().subscribe())
        } else {
            newState = PlayButtonState.STOPPED
            disposable.add(interactor.stop().subscribe()) // TODO: disposable, error-handling
        }
        playButtonState.postValue(newState)
    }

    fun onBackPressed() {
        navigator.onBackPressed()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}
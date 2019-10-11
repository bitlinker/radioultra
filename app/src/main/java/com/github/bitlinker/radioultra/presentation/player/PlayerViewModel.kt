package com.github.bitlinker.radioultra.presentation.player

import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.business.ui.PlayerViewInteractor
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.domain.PlayerStatus
import com.github.bitlinker.radioultra.domain.StreamInfo
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.github.bitlinker.radioultra.presentation.common.BaseViewModel
import com.github.bitlinker.radioultra.presentation.common.ErrorDisplayerMgr
import timber.log.Timber

// TODO: databinding hates nulls! - check metadata!
// TODO: remove databinding, use viewbinding from Jake!

class PlayerViewModel(private val navigator: PlayerViewNavigator,
                      private val interactor: PlayerViewInteractor,
                      private val schedulerProvider: SchedulerProvider,
                      private val errorDisplayerMgr: ErrorDisplayerMgr
) : BaseViewModel() {
    private val metadata = MutableLiveData<TrackMetadata>()

    val title = Transformations.map(metadata) { it.title } as LiveData<String?>
    val artist = Transformations.map(metadata) { it.artist } as LiveData<String?>
    val coverUrl = Transformations.map(metadata) { it.coverLink } as LiveData<String?>

    val playButtonState = MutableLiveData<PlayButtonState>()
    val streamInfo = MutableLiveData<StreamInfo>()

    init {
        playButtonState.value = PlayButtonState.STOPPED

        interactor.getTrackMetadata()
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        {
                            Timber.d("Metadata: $it")
                            metadata.value = it
                        },
                        { errorDisplayerMgr.showError(it) }
                )
                .connect()

        interactor.getState()
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        {
                            playButtonState.value = when (it.state) {
                                PlayerStatus.State.STOPPED -> PlayButtonState.STOPPED
                                PlayerStatus.State.STOPPED_ERROR -> {
                                    errorDisplayerMgr.showError(it.throwable)
                                    PlayButtonState.STOPPED
                                }
                                PlayerStatus.State.BUFFERING -> PlayButtonState.BUFFERING
                                PlayerStatus.State.PLAYING -> PlayButtonState.PLAYING
                            }
                        },
                        { errorDisplayerMgr.showError(it) }
                )
                .connect()

        interactor.getCurrentStreamInfo()
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        { streamInfo.value = it },
                        { errorDisplayerMgr.showError(it) }
                )
                .connect()
    }

    fun onHistoryClicked() = navigator.navigateToHistory()

    fun onChooseStreamClicked() {
        interactor.getStreamSelectionArgs()
                .subscribe(
                        { navigator.showChooseStreamDialog(it) },
                        { errorDisplayerMgr.showError(it) }
                )
                .connect()
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
        interactor.togglePlayStop()
                .subscribe(
                        {},
                        { errorDisplayerMgr.showError(it) }
                )
                .connect()
    }

    fun onBackPressed() {
        navigator.onBackPressed()
    }
}
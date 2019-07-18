package com.github.bitlinker.radioultra.presentation.trackview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.bitlinker.radioultra.domain.TrackMetadata

class TrackViewModel(private val trackViewNavigator: TrackViewNavigator,
                     private val item: TrackMetadata) : ViewModel() {
    private val metadata = MutableLiveData<TrackMetadata>()

    val title = Transformations.map(metadata) { it.title } as LiveData<String?>
    val stream = Transformations.map(metadata) { it.streamTitle } as LiveData<String?>
    val artist = Transformations.map(metadata) { it.artist } as LiveData<String?>
    val album = Transformations.map(metadata) { it.album } as LiveData<String?>
    val coverUrl = Transformations.map(metadata) { it.coverLink } as LiveData<String?>

    init {
        metadata.value = item
    }

    fun onGoogleMusicClicked() {
        if (item.googleLink != null) {
            trackViewNavigator.navigateToGoogleMusic(item.googleLink)
        }
    }

    fun onYouTubeClicked() {
        if (item.youtubeLink != null) {
            trackViewNavigator.navigateToGoogleMusic(item.youtubeLink)
        }
    }

    fun onITunesClicked() {
        if (item.itunesLink != null) {
            trackViewNavigator.navigateToGoogleMusic(item.itunesLink)
        }
    }

    // TODO: rename all same methods
    fun onBackPressed() = trackViewNavigator.navigateBack()
}
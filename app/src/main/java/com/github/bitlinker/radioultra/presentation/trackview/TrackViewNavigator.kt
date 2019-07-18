package com.github.bitlinker.radioultra.presentation.trackview

import android.net.Uri
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator

class TrackViewNavigator(private val mainNavigator: MainNavigator) {

    fun navigateToITunes(url: String) {
        mainNavigator.navigateToExternalUri(Uri.parse(url))
    }

    fun navigateToGoogleMusic(url: String) {
        mainNavigator.navigateToExternalUri(Uri.parse(url))
    }

    fun navigateToYouTube(url: String) {
        mainNavigator.navigateToExternalUri(Uri.parse(url))
    }

    fun navigateBack() {
        mainNavigator.navigateBack()
    }
}
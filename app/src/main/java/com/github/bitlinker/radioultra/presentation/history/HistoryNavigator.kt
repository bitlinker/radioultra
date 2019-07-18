package com.github.bitlinker.radioultra.presentation.history

import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator

class HistoryNavigator(val mainNavigator: MainNavigator) {

    fun navigateToItem(item: TrackMetadata) {
        mainNavigator.navigateTo(HistoryFragmentDirections.actionHistoryFragmentToTrackViewFragment(item))
    }

    fun navigateBack() {
        mainNavigator.navigateBack()
    }
}
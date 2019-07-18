package com.github.bitlinker.radioultra.presentation.player

import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator
import com.github.bitlinker.radioultra.presentation.streamselection.StreamSelectionArgs

class PlayerNavigator(val navigator: MainNavigator) {
    fun navigateToHistory() {
        navigator.navigateTo(R.id.action_playerFragment_to_historyFragment)
    }

    fun navigateToSettings() {
        navigator.navigateTo(R.id.action_playerFragment_to_settingsFragment)
    }

    fun showChooseStreamDialog(args: StreamSelectionArgs) {
        navigator.navigateTo(PlayerFragmentDirections.actionPlayerFragmentToStreamSelectionDialogFragment(args))
    }

    fun navigateToTrackInfo(item: TrackMetadata) {
        navigator.navigateTo(PlayerFragmentDirections.actionPlayerFragmentToTrackViewFragment(item))
    }

    fun onBackPressed() {
        navigator.navigateBack()
    }
}
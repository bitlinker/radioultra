package com.github.bitlinker.radioultra.presentation.player

import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator

class PlayerNavigator(val navigator: MainNavigator) {
    fun navigateToHistory() {
        navigator.navigateTo(R.id.action_playerFragment_to_historyFragment)
    }

    fun navigateToSettings() {
        navigator.navigateTo(R.id.action_playerFragment_to_settingsFragment)
    }

    fun showChooseStreamDialog() {
        navigator.navigateTo(R.id.action_playerFragment_to_streamSelectionDialogFragment)
    }

    fun onBackPressed() {
        navigator.navigateBack()
    }
}
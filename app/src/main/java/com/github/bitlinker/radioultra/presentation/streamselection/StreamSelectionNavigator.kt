package com.github.bitlinker.radioultra.presentation.streamselection

import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator

class StreamSelectionNavigator(private val mainNavigator: MainNavigator) {
    fun navigateBack() {
        mainNavigator.navigateBack()
    }
}
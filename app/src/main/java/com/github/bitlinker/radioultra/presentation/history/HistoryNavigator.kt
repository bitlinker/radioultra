package com.github.bitlinker.radioultra.presentation.history

import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator

class HistoryNavigator(val mainNavigator: MainNavigator) {
    fun onBackPressed() {
        mainNavigator.navigateBack()
    }
}
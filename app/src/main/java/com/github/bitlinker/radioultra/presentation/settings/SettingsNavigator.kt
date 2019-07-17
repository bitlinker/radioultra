package com.github.bitlinker.radioultra.presentation.settings

import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator

class SettingsNavigator(val mainNavigator: MainNavigator) {
    fun onBackPressed() = mainNavigator.navigateBack()
}
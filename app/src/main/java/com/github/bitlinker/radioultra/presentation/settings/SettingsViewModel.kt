package com.github.bitlinker.radioultra.presentation.settings

import androidx.lifecycle.ViewModel
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator

class SettingsViewModel(val navigator: SettingsNavigator) : ViewModel() {
    init {

    }

    fun onBackPressed() = navigator.onBackPressed()
}
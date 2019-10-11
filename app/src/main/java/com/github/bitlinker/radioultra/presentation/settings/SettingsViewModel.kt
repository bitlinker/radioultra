package com.github.bitlinker.radioultra.presentation.settings

import com.github.bitlinker.radioultra.business.ui.SettingsViewInteractor
import com.github.bitlinker.radioultra.presentation.common.BaseViewModel

class SettingsViewModel(
        private val navigator: SettingsNavigator,
        private val settingsViewInteractor: SettingsViewInteractor
) : BaseViewModel() {

    fun getVersionString(): String {
        return settingsViewInteractor.getVersionString()
    }

    fun onBackPressed() = navigator.onBackPressed()
}
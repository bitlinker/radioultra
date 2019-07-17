package com.github.bitlinker.radioultra.presentation.navigation

import androidx.annotation.IdRes
import androidx.navigation.NavDirections

class MainNavigatorMgr : MainNavigator {
    var navigator: MainNavigator? = null

    override fun navigateBack() {
        navigator?.navigateBack()
    }

    override fun navigateTo(@IdRes id: Int) {
        navigator?.navigateTo(id)
    }

    override fun navigateTo(directions: NavDirections) {
        navigator?.navigateTo(directions)
    }
}
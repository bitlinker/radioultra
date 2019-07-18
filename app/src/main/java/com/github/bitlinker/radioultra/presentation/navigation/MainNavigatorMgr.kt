package com.github.bitlinker.radioultra.presentation.navigation

import android.net.Uri
import androidx.annotation.IdRes
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator

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

    override fun navigateTo(directions: NavDirections, extras: FragmentNavigator.Extras?) {
        navigator?.navigateTo(directions, extras)
    }

    override fun navigateToExternalUri(uri: Uri) {
        navigator?.navigateToExternalUri(uri)
    }
}
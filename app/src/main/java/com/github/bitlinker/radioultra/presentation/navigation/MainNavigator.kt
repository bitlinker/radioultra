package com.github.bitlinker.radioultra.presentation.navigation

import android.net.Uri
import androidx.annotation.IdRes
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator

interface MainNavigator {
    fun navigateTo(@IdRes id: Int)
    fun navigateTo(directions: NavDirections)
    fun navigateTo(directions: NavDirections, extras: FragmentNavigator.Extras?)
    fun navigateToExternalUri(uri: Uri)
    fun navigateBack()
}
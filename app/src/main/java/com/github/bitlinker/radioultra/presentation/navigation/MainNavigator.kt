package com.github.bitlinker.radioultra.presentation.navigation

import androidx.annotation.IdRes
import androidx.navigation.NavDirections

interface MainNavigator {
    fun navigateTo(@IdRes id: Int);
    fun navigateTo(directions: NavDirections);
    fun navigateBack();

}
package com.github.bitlinker.radioultra.business.ui

import com.github.bitlinker.radioultra.data.PackageUtils

class SettingsViewInteractor(private val packageUtils: PackageUtils) {
    fun getVersionString(): String {
        return packageUtils.getVersionString()
    }
}
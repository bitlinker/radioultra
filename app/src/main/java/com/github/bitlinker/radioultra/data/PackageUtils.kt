package com.github.bitlinker.radioultra.data

import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber

class PackageUtils(private val context: Context) {
    fun getVersionString(): String {
        try {
            val pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "Can't get package version")
        }
        return ""
    }
}
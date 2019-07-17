package com.github.bitlinker.radioultra.presentation

import android.content.Context
import androidx.annotation.StringRes

// TODO: rm if unused
class ResourceProvider(private val context: Context) {
    fun getString(@StringRes resId: Int) : String{
        return context.getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any) : String{
        return context.getString(resId, formatArgs)
    }
}
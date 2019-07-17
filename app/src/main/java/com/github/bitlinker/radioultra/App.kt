package com.github.bitlinker.radioultra

import android.app.Application
import com.github.bitlinker.radioultra.di.Injector
import com.squareup.picasso.Picasso
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initDi()
        initLogging()
    }

    fun initDi() {
        Injector.instance.init(this)
    }

    fun initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
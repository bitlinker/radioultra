package com.github.bitlinker.radioultra.data.schedulers

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AndroidSchedulerProvider : SchedulerProvider {
    override fun io() = Schedulers.io()

    override fun computation() = Schedulers.computation()

    override fun ui() = AndroidSchedulers.mainThread()
}
package com.github.bitlinker.radioultra.data.wrappers

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import timber.log.Timber

class WakelockWrapper(private val context: Context,
                      private val uiScheduler: Scheduler) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    @SuppressLint("WakelockTimeout")
    fun aquirePlayerLock(tag: String): Completable {
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag)
        return Completable.never()
                .doOnSubscribe {
                    wakeLock.acquire()
                    Timber.d("Wakelock acquired: $wakeLock")
                }
                .doFinally {
                    wakeLock.release()
                    Timber.d("Wakelock released: $wakeLock")
                }
                .subscribeOn(uiScheduler)
    }
}
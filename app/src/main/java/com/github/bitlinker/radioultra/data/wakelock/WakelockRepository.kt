package com.github.bitlinker.radioultra.data.wakelock

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import io.reactivex.Completable

class WakelockRepository(private val context: Context) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val playerWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.packageName + "_playerWakeLock")

    @SuppressLint("WakelockTimeout")
    fun aquirePlayerLock(): Completable {
        return Completable.never()
                .doOnSubscribe { playerWakeLock.acquire() }
                .doFinally { playerWakeLock.release() }
    }
}
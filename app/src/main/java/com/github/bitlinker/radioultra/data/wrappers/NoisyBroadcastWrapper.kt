package com.github.bitlinker.radioultra.data.wrappers

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import io.reactivex.Observable
import io.reactivex.Scheduler

class NoisyBroadcastWrapper(
        private val context: Context,
        private val schedulerProvider: SchedulerProvider) {
    fun noisyBroadcastReceiverObservable(): Observable<Boolean> {
        return broadcastReceiverObservable(context, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), schedulerProvider.ui())
                .filter { AudioManager.ACTION_AUDIO_BECOMING_NOISY == it.action }
                .map { true }
    }
}

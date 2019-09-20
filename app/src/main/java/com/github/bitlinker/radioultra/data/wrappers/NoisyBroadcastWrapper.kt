package com.github.bitlinker.radioultra.data.wrappers

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import io.reactivex.Observable
import io.reactivex.Scheduler

fun noisyBroadcastReceiverObservable(context: Context, uiScheduler: Scheduler): Observable<Boolean> {
    return broadcastReceiverObservable(context, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), uiScheduler)
            .filter { AudioManager.ACTION_AUDIO_BECOMING_NOISY == it.action }
            .map { true }
}

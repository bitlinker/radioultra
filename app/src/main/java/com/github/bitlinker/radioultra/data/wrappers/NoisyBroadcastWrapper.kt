package com.github.bitlinker.radioultra.data.wrappers

import android.content.IntentFilter
import android.media.AudioManager
import io.reactivex.Observable


class NoisyBroadcastWrapper(private val broadcastReceiverWrapper: BroadcastReceiverWrapper) {

    fun noisyBroadcastObservable(): Observable<Boolean> {
        return broadcastReceiverWrapper.broadcastReceiverObservable(IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
                .filter { AudioManager.ACTION_AUDIO_BECOMING_NOISY == it.action }
                .map { true }
    }
}
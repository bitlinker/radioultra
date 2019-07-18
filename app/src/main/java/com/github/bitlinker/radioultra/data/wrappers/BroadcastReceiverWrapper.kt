package com.github.bitlinker.radioultra.data.wrappers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BroadcastReceiverWrapper(private val context: Context,
                               private val uiScheduler: Scheduler) {
    fun broadcastReceiverObservable(intentFilter: IntentFilter): Observable<Intent> {
        val receiver = object : BroadcastReceiver() {
            val subject = PublishSubject.create<Intent>()
            override fun onReceive(context: Context, intent: Intent) {
                subject.onNext(intent)
            }
        }
        return receiver.subject
                .doOnSubscribe {
                    context.registerReceiver(receiver, intentFilter)
                    Timber.d("Receiver registered: $receiver, filter: $intentFilter")
                }
                .doFinally {
                    context.unregisterReceiver(receiver)
                    Timber.d("Receiver unregistered: $receiver")
                }
                .subscribeOn(uiScheduler)
    }
}
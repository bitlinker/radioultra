package com.github.bitlinker.radioultra.data.playerservice

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe

fun <T : IBinder> Context.bindServiceObservable(intent: Intent, flags: Int = Service.BIND_AUTO_CREATE): Observable<T> {
    return Observable.using(
            { ConnectionObservable<T>() },
            {
                this.bindService(intent, it, flags)
                Observable.create(it)
            },
            {
                this.unbindService(it)
            }
    )
}

private class ConnectionObservable<T : IBinder> : ServiceConnection, ObservableOnSubscribe<T> {
    private var subscriber: ObservableEmitter<T>? = null

    override fun onServiceDisconnected(name: ComponentName?) {
        subscriber?.apply {
            if (!isDisposed) this.onComplete()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        subscriber?.apply {
            @Suppress("UNCHECKED_CAST")
            if (!isDisposed && service != null) this.onNext(service as T)
        }
    }

    override fun subscribe(emitter: ObservableEmitter<T>) {
        subscriber = emitter
    }
}

package com.github.bitlinker.radioultra.data.wrappers

import android.content.Context
import android.media.AudioManager
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import timber.log.Timber

class AudioFocusWrapper(context: Context) {
    enum class State {
        GAIN,
        LOSS,
        LOSS_TRANSIENT,
        LOSS_TRANSIENT_CAN_DUCK
    }

    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    fun request(): Observable<State> {
        val requestBuilder = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
                .setAudioAttributes(AudioAttributesCompat.Builder()
                        .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build()
                )
                .setWillPauseWhenDucked(true)

        return Observable.using(
                { AudioFocusStateObservable(requestBuilder) },
                {
                    if (!it.acquire()) {
                        Timber.w("Failed to acquire initial audiofocus")
                        Observable.just(State.LOSS)
                    } else Observable.create(it)
                },
                { it.release() }
        )
    }

    private inner class AudioFocusStateObservable(builder: AudioFocusRequestCompat.Builder) : ObservableOnSubscribe<State>, AudioManager.OnAudioFocusChangeListener {
        private var subscriber: ObservableEmitter<State>? = null
        private val request = builder
                .setOnAudioFocusChangeListener(this)
                .build()

        fun acquire(): Boolean {
            Timber.d("Acquiring audiofocus")
            if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED != AudioManagerCompat.requestAudioFocus(audioManager, request)) {
                Timber.w("Failed to acquire initial audiofocus")
                return false
            }
            return true
        }

        fun release() {
            Timber.d("Releasing audiofocus")
            AudioManagerCompat.abandonAudioFocusRequest(audioManager, request)
        }

        override fun onAudioFocusChange(focusChange: Int) {
            // Where are no such constants in AudioManagerCompat...
            val state = when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> State.GAIN
                AudioManager.AUDIOFOCUS_LOSS -> State.LOSS
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> State.LOSS_TRANSIENT
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> State.LOSS_TRANSIENT_CAN_DUCK
                else -> throw IllegalStateException("Wrong audiofocus state: $focusChange")
            }
            Timber.d("Audiofocus changed: $state")
            subscriber?.apply {
                if (!isDisposed) {
                    onNext(state)
                }
            }
        }

        override fun subscribe(emitter: ObservableEmitter<State>) {
            subscriber = emitter
        }
    }
}
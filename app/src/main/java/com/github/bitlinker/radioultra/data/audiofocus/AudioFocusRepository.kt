package com.github.bitlinker.radioultra.data.audiofocus

import android.content.Context
import android.media.AudioManager
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.lang.IllegalStateException

class AudioFocusRepository(context: Context) {
    enum class State {
        GAIN,
        LOSS,
        LOSS_TRANSIENT,
        LOSS_TRANSIENT_CAN_DUCK
    }

    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    private val focusState = BehaviorSubject.create<State>().toSerialized()

    // TODO: AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK or EXCLUSIVE
    private val request = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributesCompat.Builder()
                    .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC) // TODO: needed?
                    .build()
            )
            .setOnAudioFocusChangeListener {
                // Where are no such constants in AudioManagerCompat O_o
                val state = when (it) {
                    AudioManager.AUDIOFOCUS_GAIN -> State.GAIN
                    AudioManager.AUDIOFOCUS_LOSS -> State.LOSS
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> State.LOSS_TRANSIENT
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> State.LOSS_TRANSIENT_CAN_DUCK
                    else -> throw IllegalStateException("Wrong audiofocus state: $it")
                }
                focusState.onNext(state)
            }
            .setWillPauseWhenDucked(true)
            .build()

    fun request(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == AudioManagerCompat.requestAudioFocus(audioManager, request)
    }

    fun release(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == AudioManagerCompat.abandonAudioFocusRequest(audioManager, request)
        // TODO: apply state here?
    }

    // TODO: where to subscribe?
    fun state(): Observable<State> = focusState
}
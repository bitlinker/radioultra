package com.github.bitlinker.radioultra.data.mediasession

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class MediaControllerWrapper(private val context: Context, token: MediaSessionCompat.Token) {
    val mediaControllerCompat = MediaControllerCompat(context, token)
    val transportControls = mediaControllerCompat.transportControls

    fun status() {
        mediaControllerCompat.registerCallback(object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                super.onPlaybackStateChanged(state)

            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                super.onMetadataChanged(metadata)
            }

            override fun onAudioInfoChanged(info: MediaControllerCompat.PlaybackInfo?) {
                super.onAudioInfoChanged(info)
            }
        })
        // TODO
        //mediaControllerCompat.unregisterCallback()
    }

    fun play() {
        transportControls.play()
    }

    fun stop() {
        transportControls.stop()
    }

    fun setStream() {
        // TODO
    }
}
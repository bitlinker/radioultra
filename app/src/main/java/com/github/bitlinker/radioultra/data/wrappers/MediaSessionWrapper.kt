package com.github.bitlinker.radioultra.data.wrappers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.data.playerservice.PlayerService
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.domain.PlayerStatus
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import timber.log.Timber
import java.io.Closeable
import java.io.IOException
import java.lang.Exception

class MediaSessionWrapper(private val context: Context,
                          private val schedulerProvider: SchedulerProvider) : Closeable {
    interface ControllerCallback {
        fun play()
        fun stop()
    }

    private val mediaSessionTag = "${context.packageName}_MediaSession"
    private val mediaSession = MediaSessionCompat(context, mediaSessionTag)
    private val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON, null, context, PlayerService::class.java)
    private val placeholderCoverImage: Bitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.drawable.ultralogo)
    }

    var controllerCallback: ControllerCallback? = null

    init {
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(context, 0, mediaButtonIntent, 0))

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                controllerCallback?.play()
            }

            override fun onPlay() {
                controllerCallback?.play()
            }

            override fun onStop() {
                controllerCallback?.stop()
            }

            override fun onPause() {
                controllerCallback?.stop()
            }
        })
    }

    fun setActive(isActive: Boolean) {
        mediaSession.isActive = isActive
    }

    fun setMetadata(metadata: TrackMetadata): Completable {
        return getCoverBitmapOrPlaceholder(metadata.coverLink)
                .flatMapCompletable {
                    Completable.fromRunnable {
                        val builder = MediaMetadataCompat.Builder()

                        val title = if (!metadata.title.isNullOrEmpty()) metadata.title
                        else if (!metadata.streamTitle.isNullOrEmpty()) metadata.streamTitle
                        else context.getString(R.string.app_name)

                        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)

                        metadata.artist?.apply {
                            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this)
                        }

                        metadata.album?.apply {
                            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, this)
                        }

                        metadata.coverLink?.apply {
                            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, this)
                        }

                        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it)

                        mediaSession.setMetadata(builder.build())
                    }
                }
    }

    private fun getCoverBitmapOrPlaceholder(url: String?): Single<Bitmap> {
        return Maybe.defer {
            if (url.isNullOrEmpty()) return@defer Maybe.empty<Bitmap>()

            Maybe.create<Bitmap> {
                val target = object : Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        // Do nothing...
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        it.onError(e ?: IOException("Can't load image: $url"))
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        it.onSuccess(bitmap!!)
                    }
                }
                Picasso.get().load(url).into(target)
            }
        }
                .switchIfEmpty(Single.just(placeholderCoverImage))
                .doOnError { Timber.e(it, "Picasso error: ") }
                .onErrorResumeNext(Single.just(placeholderCoverImage))
                .subscribeOn(schedulerProvider.ui())

    }

    fun setPlaybackState(playerStatus: PlayerStatus) {
        val builder = PlaybackStateCompat.Builder()
        when (playerStatus.state) {
            PlayerStatus.State.BUFFERING -> {
                builder.setActions(PlaybackStateCompat.ACTION_STOP)
                builder.setState(PlaybackStateCompat.STATE_BUFFERING, -1L, 1F)
            }
            PlayerStatus.State.PLAYING -> {
                builder.setActions(PlaybackStateCompat.ACTION_STOP)
                builder.setState(PlaybackStateCompat.STATE_PLAYING, -1L, 1F)
            }
            PlayerStatus.State.STOPPED -> {
                builder.setActions(PlaybackStateCompat.ACTION_PLAY)
                builder.setState(PlaybackStateCompat.STATE_STOPPED, -1L, 1F)
            }
            PlayerStatus.State.STOPPED_ERROR -> {
                builder.setActions(PlaybackStateCompat.ACTION_PLAY)
                builder.setState(PlaybackStateCompat.STATE_STOPPED, -1L, 1F)
                builder.setErrorMessage(PlaybackStateCompat.ERROR_CODE_UNKNOWN_ERROR, context.getString(R.string.error_playback_error))
            }
        }
        mediaSession.setPlaybackState(builder.build())
    }

    fun getToken(): MediaSessionCompat.Token {
        return mediaSession.sessionToken
    }

    fun getController(): MediaControllerCompat {
        return mediaSession.controller
    }

    fun getSession(): MediaSessionCompat {
        return mediaSession
    }

    override fun close() {
        mediaSession.setCallback(null)
        mediaSession.release()
    }
}
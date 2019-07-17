package com.github.bitlinker.radioultra.data.mediasession

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.github.bitlinker.radioultra.domain.PlayerStatus
import com.github.bitlinker.radioultra.domain.TrackMetadata
import java.io.Closeable

class MediaSessionRepository(private val context: Context) : Closeable {
    // TODO: create media session explicitly
    private val mediaSessionTag = "${context.packageName}_MediaSession"

    private val mediaSession = MediaSessionCompat(context, mediaSessionTag)

    init {
        // TODO: use it to control from notification:

        // Controller is created inside session:
        //mediaSession.controller
        // This is used to control playback from external ui. probalby not needed here
        //mediaController.transportControls.play()
//        mediaController.registerCallback(object: MediaControllerCompat.Callback() {
//            // This is needed to modify extrernal ui. Probably not needed here
//        })

        mediaSession.isActive = true
        //mediaSession.sessionToken - pass to notification

        // TODO: call in the end
        //mediaSession.release()

        // This updates session state:
        //mediaSession.setMetadata()
        //mediaSession.setPlaybackState()
        // TODO:??? Intent#ACTION_MEDIA_BUTTON
        //mediaSession.setMediaButtonReceiver()
        //mediaSession.setSessionActivity()

        // Extrnal controls callback
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
            }

            override fun onStop() {
                super.onStop()
            }

            override fun onPause() {
                super.onPause()
            }
        })
    }

    fun setActive(isActive: Boolean) {
        mediaSession.isActive = isActive
    }

    fun setMetadata(metadata: TrackMetadata) {
        val builder = MediaMetadataCompat.Builder()
        if (metadata.title != null)
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadata.title)

        if (metadata.artist != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadata.artist)
        }

        // TODO: set title and subtitle manually?
        // TODO: more fields
        builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "testTitle!")
        builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "testSubtitle!")

        mediaSession.setMetadata(builder.build())
    }

    fun setPlaybackState(playerStatus: PlayerStatus) {
        // TODO: mapping
        val builder = PlaybackStateCompat.Builder()
        builder.setActions(PlaybackStateCompat.ACTION_PLAY) // TODO: more?
        //builder.setErrorMessage() // TODO: if error
        builder.setState(PlaybackStateCompat.STATE_PLAYING, 0L, 1F)
        mediaSession.setPlaybackState(builder.build())
    }

    fun getToken(): MediaSessionCompat.Token {
        return mediaSession.sessionToken
    }

    // TODO: call me
    override fun close() {
        mediaSession.release()
    }
}

// TODO: MediaBrowserService too?
// Need to implement MediaBrowserService()

//class PlayerMediaBrowserService : MediaBrowserService() {
//    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowser.MediaItem>>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    fun sessionFun() {
//
//    }
//
//}
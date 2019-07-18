package com.github.bitlinker.radioultra.data.playerservice

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.business.notification.PlayerServiceController
import com.github.bitlinker.radioultra.di.Injector
import com.github.bitlinker.radioultra.domain.PlayerStatus
import com.github.bitlinker.radioultra.domain.TrackMetadata
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.scope.Scope
import timber.log.Timber

/**
 * This service is used to keep application alive during playback & mantain notification
 * Notification control intents are also handled here
 *
 * acts like viewmodel here...
 */

private const val ROOT_MEDIA_ID = "ROOT_MEDIA_ID"

class PlayerService : MediaBrowserServiceCompat() {
    inner class LocalBinder : Binder() {
        fun getService() = object : PlayerServiceInterface {
            override fun play() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun stop() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun pause() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun playerStatus(): Observable<PlayerStatus> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun metadata(): Observable<TrackMetadata> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }

    private lateinit var playerScope: Scope
    private lateinit var controller: PlayerServiceController
    private val binder = LocalBinder()


    // TODO: use controller here
    // TODO: stop service when no ui & notification destroyed? or on cancel?

    // Stop with notification cancellation event
    // Or when exiting from ui with not playing state

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate");
        playerScope = Injector.instance.openPlayerScope("player_scope_id") // TODO: id here

        controller = playerScope.get()

        sessionToken = controller.getMediaSessionRepo().getToken()
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        if (parentId != ROOT_MEDIA_ID) {
            result.sendResult(mutableListOf())
            return
        }

        val iconRes = R.drawable.ultralogo
        val descriptionBuilder = MediaDescriptionCompat.Builder()
                .setTitle(getString(R.string.app_name))
                .setIconUri(Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(resources.getResourcePackageName(iconRes))
                        .appendPath(resources.getResourceTypeName(iconRes))
                        .appendPath(resources.getResourceEntryName(iconRes))
                        .build()
                )
                .setMediaId(controller.getMediaId())

        val item = MediaBrowserCompat.MediaItem(descriptionBuilder.build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
        result.sendResult(mutableListOf(item))
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(ROOT_MEDIA_ID, null)
    }

    override fun onDestroy() {
        Timber.d("dispose");
        controller.dispose()
        playerScope.close()
        super.onDestroy()
    }
}
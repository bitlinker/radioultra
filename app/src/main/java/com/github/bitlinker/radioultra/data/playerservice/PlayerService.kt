package com.github.bitlinker.radioultra.data.playerservice

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.business.playerservice.PlayerServiceApi
import com.github.bitlinker.radioultra.business.playerservice.PlayerServiceInteractor
import com.github.bitlinker.radioultra.di.getOrCreateObjectScope
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.getKoin
import org.koin.core.scope.Scope
import timber.log.Timber


/**
 * This service is used to keep application alive during playback & maintain foreground control notification
 */

private const val ROOT_MEDIA_ID = "ROOT_MEDIA_ID"
private const val RADIO_MEDIA_ID = "RADIO_MEDIA_ID"

class PlayerService : MediaBrowserServiceCompat() {
    inner class LocalBinder : Binder() {
        fun getServiceApi() = interactor as PlayerServiceApi
    }

    private lateinit var scope: Scope
    private lateinit var interactor: PlayerServiceInteractor
    private val disposable = CompositeDisposable()

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        scope = getOrCreateObjectScope(getKoin())
        interactor = scope.get(PlayerServiceInteractor::class.java)
        super.onCreate()
        Timber.d("onCreate");

        disposable.add(
                interactor.bindToService(this)
                        .subscribe()
        )

        sessionToken = interactor.getMediaSessionNow().sessionToken
    }

    fun startMe() {
        Timber.d("Starting self")
        startService(Intent(applicationContext, PlayerService::class.java))
    }

    fun stopMe() {
        Timber.d("Stopping self")
        stopSelf()
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
                .setMediaId(RADIO_MEDIA_ID)

        val item = MediaBrowserCompat.MediaItem(descriptionBuilder.build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
        result.sendResult(mutableListOf(item))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(interactor.getMediaSessionNow(), intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(ROOT_MEDIA_ID, null)
    }

    override fun onDestroy() {
        Timber.d("onDestroy");
        disposable.clear()
        scope.close()
        super.onDestroy()
    }
}
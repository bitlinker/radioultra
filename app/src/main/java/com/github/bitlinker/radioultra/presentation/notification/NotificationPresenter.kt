package com.github.bitlinker.radioultra.presentation.notification

import android.app.Notification
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.core.app.NotificationCompat
import android.os.Build
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.Service
import android.media.session.MediaSession
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.media.session.MediaButtonReceiver
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.domain.PlayerStatus
import com.github.bitlinker.radioultra.domain.StreamMetadata
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.squareup.picasso.Picasso

class NotificationPresenter(private val context: Context,
                            private val picasso: Picasso) {
    val ID = 1005

    val manager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private fun createNotificationChannel(): String {
        val channelId: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannelO(
                    "RADIOMAN_ID", // TODO: package
                    "Radioman",
                    "Radioman player notification channel") // TODO: resources
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            channelId = ""
        }
        return channelId;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelO(id: String,
                                           name: String,
                                           description: String): String {
        val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_NONE)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        channel.description = description

        manager.createNotificationChannel(channel) // TODO: handle error
        return id
    }

    fun startForeground(service: Service, token: MediaSessionCompat.Token) {
        service.startForeground(ID, createNotification(token))
    }

    fun stopForeground(service: Service) {
        service.stopForeground(true)
    }

    // TODO: not use if can be controlled with mediasession!
    // keep some flag if notification visible
//    fun update(trackMetadata: TrackMetadata) {
//    }

    fun createNotification(token: MediaSessionCompat.Token): Notification {
        val style = androidx.media.app.NotificationCompat.MediaStyle()
        style.setMediaSession(token)

//        val playAction = NotificationCompat.Action(
//                R.drawable.ic_play_arrow_black_24dp,
//                "play", // TODO: text
//                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
//        );

        // TODO: store builder to modify notification state
        return NotificationCompat.Builder(context, createNotificationChannel())
                .setAutoCancel(false)
                //.setContentTitle("Artist - track")
                //.setContentText("Radio ULTRA")
                //.setOngoing(true) // TODO
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .setStyle(style)
                //.setStyle(NotificationCompat.BigTextStyle())
                //.setLargeIcon((context.getDrawable(R.mipmap.ic_launcher) as BitmapDrawable).bitmap)
                //.setSubText("status here")
                .setSmallIcon(R.mipmap.ic_launcher) // TODO: transparent?
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                //.setContentIntent(createContentIntent()) // TODO
                //.addAction(stopAction)
                //.addAction(playAction)
                //.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
                .build()!!
    }

//
//    fun updateNotification(metadata: TrackMetadata, playerStatus: PlayerStatus): Notification {
//        val style = androidx.media.app.NotificationCompat.MediaStyle()
//
//        // TODO: MediaSession here
//        //style.setMediaSession(mediaSession.getSessionToken())
//
//        //if (playerStatus.state == PlayerStatus.State.PLAYING)
//        val playAction = NotificationCompat.Action(
//                R.drawable.ic_play_arrow_black_24dp,
//                "play", // TODO: text
//                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
//        );
//
//        //metadata.title
//
//        // TODO: store builder to modify notification state
//        return NotificationCompat.Builder(context, createNotificationChannel())
//                .setAutoCancel(false)
//                .setContentTitle("Artist - track")
//                .setContentText("Radio ULTRA")
//                .setOngoing(true) // TODO
//                .setCategory(Notification.CATEGORY_SERVICE)
//                .setOnlyAlertOnce(true)
//                .setStyle(style)
//                .setLargeIcon((context.getDrawable(R.mipmap.ic_launcher) as BitmapDrawable).bitmap)
//                //.setSubText("status here")
//                .setSmallIcon(R.drawable.ic_settings_black_24dp) // TODO: transparent?
//                //.addAction(stopAction)
//                //.addAction(playAction)
//                //.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
//                .build()!!
//    }
}
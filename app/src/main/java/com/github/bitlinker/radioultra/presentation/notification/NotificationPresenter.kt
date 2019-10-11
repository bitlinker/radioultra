package com.github.bitlinker.radioultra.presentation.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.github.bitlinker.radioultra.BuildConfig
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.data.wrappers.MediaSessionWrapper
import com.github.bitlinker.radioultra.presentation.MainActivity

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + "_player_notification_channel"
private const val NOTIFICATION_ID = 1

class NotificationPresenter(private val context: Context,
                            private val sessionWrapper: MediaSessionWrapper) {
    private val manager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private val playAction = NotificationCompat.Action(
            R.drawable.ic_play_arrow_black_24dp,
            context.getString(R.string.notification_player_action_play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
    )

    val mainActivityIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)

    // TODO: replace stop with pause (system media session UI works so)
    private val stopAction = NotificationCompat.Action(
            R.drawable.ic_stop_black_24dp,
            context.getString(R.string.notification_player_action_stop),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)
    )

    private val notificationChannelId by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.notification_player_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.description = context.getString(R.string.notification_player_channel_description)
            manager.createNotificationChannel(channel)
            channel.id
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            ""
        }
    }

    fun startForeground(service: Service) {
        service.startForeground(NOTIFICATION_ID, buildNotification())
    }

    fun stopForeground(service: Service, removeNotification: Boolean) {
        service.stopForeground(removeNotification)
    }

    fun updateNotification() {
        val notificationPresenter = buildNotification()
        manager.notify(NOTIFICATION_ID, notificationPresenter)
    }

    private fun buildNotification(): Notification {
        // Can't cache builder because there is no way to remove action from it O_o
        val controller = sessionWrapper.getController()
        val playbackState = controller.playbackState
        val mediaMetadata = controller.metadata
        val builder = NotificationCompat.Builder(context, notificationChannelId)
        builder
                //.setContentIntent(controller.sessionActivity) // TODO: how to set session activity?
                .setContentIntent(mainActivityIntent)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_ultra_arrow)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setColor(ContextCompat.getColor(context, R.color.primaryDarkColor))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)

        if (mediaMetadata != null) {
            val description = mediaMetadata.description
            builder
                    .setContentTitle(description.title)
                    .setContentText(description.subtitle)
                    .setSubText(description.description)
                    .setLargeIcon(mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
        }

        if (playbackState != null) {
            if ((playbackState.actions and PlaybackStateCompat.ACTION_PLAY) > 0) {
                builder.addAction(playAction)
                builder.setOngoing(false)
            }

            if ((playbackState.actions and PlaybackStateCompat.ACTION_PAUSE) > 0) {
                builder.addAction(stopAction)
                builder.setOngoing(true)
            }
        } else {
            builder.setOngoing(false)
        }

        val style = androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context,
                                PlaybackStateCompat.ACTION_PAUSE)
                )
                .setMediaSession(sessionWrapper.getToken())

        builder.setStyle(style)

        return builder.build()
    }
}
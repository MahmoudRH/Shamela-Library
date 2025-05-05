package com.shamela.apptheme.presentation.util.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.shamela.apptheme.R

object NotificationHelper {

    fun createChannel(context: Context, type: ChannelType) {
        val channelId = context.getString(type.id)
        val channel = NotificationChannel(
            channelId,
            context.getString(type.title),
            type.importance
        ).apply {
            description = context.getString(type.description)
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun startNotification(
        context: Context,
        title: String,
        content: String,
        type: ChannelType,
    ): Notification {
        val channelId = context.getString(type.id)
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(type.importance)
            .setVibrate(longArrayOf(1000, 1000))
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()
    }
}

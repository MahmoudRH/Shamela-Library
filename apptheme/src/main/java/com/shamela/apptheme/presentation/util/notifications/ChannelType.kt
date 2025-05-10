package com.shamela.apptheme.presentation.util.notifications

import android.app.NotificationManager
import androidx.annotation.StringRes
import com.shamela.apptheme.R

enum class ChannelType(
    @StringRes val id: Int,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val importance: Int,
) {
    BookPreparation(
        id = R.string.prepering_books,
        title = R.string.books_preperation_notificatinos,
        description = R.string.books_preperation_notification_description,
        importance = NotificationManager.IMPORTANCE_HIGH,
    ),
    DatabaseMigration(
        id = R.string.updating_database,
        title = R.string.database_update_notifications,
        description = R.string.database_update_notifications_description,
        importance = NotificationManager.IMPORTANCE_HIGH,
    ),
}
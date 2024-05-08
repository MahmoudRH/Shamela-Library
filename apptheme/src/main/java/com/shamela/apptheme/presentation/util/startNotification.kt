package com.shamela.apptheme.presentation.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.shamela.apptheme.R


enum class ChannelType(
    val id: String,
    val title: String,
    val description: String,
    val importance: Int,
) {
    BookPreparation(
        id = "تحضير الكتب",
        title = "اشعارات تحضير الكتب",
        description = "مسؤولة عن اشعارات تحضير الكتب للبحث",
        importance = NotificationManager.IMPORTANCE_HIGH,
    ),
    DatabaseMigration(
        id = "تحديث قاعدة البيانات",
        title = "اشعارات تحديث فاعدة البيانات",
        description = "مسؤولة عن اشعارات تحديث قاعدة البيانات",
        importance = NotificationManager.IMPORTANCE_HIGH,
    ),

}

fun startNotification(
    context: Context,
    title: String,
    type: ChannelType,
    content: String,
): Notification {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            type.id,
            type.title,
            type.importance
        ).apply {
            description = type.description
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    return NotificationCompat.Builder(context, type.id)
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(R.drawable.ic_notification)
        .setPriority(type.importance)
        .setVibrate(longArrayOf(1000, 1000))
        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
        .build()
}

@Composable
fun Context.RequestPermission(
    permission: String,
    onGranted: (Boolean) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        onGranted(isGranted)
    }
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            this,
            permission
        ),
        -> {
            onGranted(true)
        }

        else -> {
            SideEffect { // SideEffect just when you need to request your permission
                // first time before composition
                launcher.launch(permission)
            }
        }
    }
    SideEffect { // SideEffect just when you need to request your permission
        // first time before composition
        launcher.launch(permission)
    }
}


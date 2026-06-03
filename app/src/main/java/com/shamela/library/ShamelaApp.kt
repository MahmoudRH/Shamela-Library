package com.shamela.library

import android.app.Application
import android.content.Context
import com.shamela.apptheme.data.sharedPrefs.SharedPreferencesData
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import com.shamela.apptheme.presentation.theme.colors.AppColors
import com.shamela.apptheme.presentation.util.notifications.ChannelType
import com.shamela.apptheme.presentation.util.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class ShamelaApp : Application() {
    companion object {
        const val EXTERNAL_BOOKS_CATEGORY = "كتب خارجية"
        lateinit var externalMediaDir: File
            private set

        lateinit var externalBooksDirectory: File
            private set
    }

    override fun onCreate() {
        super.onCreate()
        externalMediaDir = externalMediaDirs.firstOrNull() ?: run {
            File(applicationContext.filesDir, "fallback_directory")
        }
        externalBooksDirectory =
            File(externalMediaDir, "ShamelaDownloads/${EXTERNAL_BOOKS_CATEGORY}")

        if (BuildConfig.DEBUG) {
            android.os.StrictMode.setThreadPolicy(
                android.os.StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            android.os.StrictMode.setVmPolicy(
                android.os.StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        AppFonts.init(this)
        val availableFontFamilies = AppFonts.getAvailableFontFamilies()
        val availableFontSizes = AppFonts.getAvailableFontSizes()
        val availableThemes = AppTheme.getAvailableThemes()
        val availableColorSchemes = AppColors.getAvailableColorSchemes()
        SharedPreferencesData(this).apply {
            saveAvailableFontFamilies(availableFontFamilies)
            saveAvailableThemes(availableThemes)
            saveAvailableFontSizes(availableFontSizes)
            saveAvailableColorSchemes(availableColorSchemes)
        }
        setupNotificationChannels(this)
    }

    private fun setupNotificationChannels(context: Context) {
        ChannelType.entries.forEach { NotificationHelper.createChannel(context, it) }
    }
}
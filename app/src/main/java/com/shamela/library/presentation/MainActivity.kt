package com.shamela.library.presentation

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.shamela.library.data.local.sharedPrefs.SharedPreferencesData
import com.shamela.library.domain.usecases.userPreferences.ReadUserPreferences
import com.shamela.library.presentation.reciever.DownloadCompleteReceiver
import com.shamela.library.presentation.screens.HomeHostScreen
import com.shamela.library.presentation.theme.AppFonts
import com.shamela.library.presentation.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val downloadCompleteReceiver = DownloadCompleteReceiver()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ReadUserPreferences(SharedPreferencesData(this)).invoke().apply {
            AppFonts.changeFontFamily(AppFonts.fontFamilyOf(fontFamily))
            AppFonts.changeFontSize(fontSize)
            AppTheme.changeColorScheme(
                AppTheme.themeOf(
                    theme,
                    colorScheme,
                    isNightMode(this@MainActivity),
                    this@MainActivity
                ), theme
            )

        }
        setContent {
            AppTheme.ShamelaLibraryTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HomeHostScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadCompleteReceiver,filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(downloadCompleteReceiver)
    }

    private fun isNightMode(context: Context): Boolean {
        val nightModeFlags =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}

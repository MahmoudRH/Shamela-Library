package com.shamela.library.presentation

import android.Manifest
import android.app.DownloadManager
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.shamela.apptheme.data.sharedPrefs.SharedPreferencesData
import com.shamela.apptheme.domain.usecases.userPreferences.ReadUserPreferences
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import com.shamela.apptheme.presentation.util.RequestPermission
import com.shamela.library.presentation.reciever.DownloadCompleteReceiver
import com.shamela.library.presentation.screens.HomeHostScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val downloadCompleteReceiver = DownloadCompleteReceiver()
    private val userPreferences = SharedPreferencesData(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReadUserPreferences(userPreferences).invoke().apply {
            AppFonts.changeFontFamily(AppFonts.fontFamilyOf(fontFamily))
            AppFonts.changeFontSize(fontSize)
            AppTheme.changeColorScheme(
                AppTheme.themeOf(
                    theme,
                    colorScheme,
                    AppTheme.isDarkTheme(this@MainActivity),
                    this@MainActivity
                ),
                theme
            )
        }
        setContent {
            AppTheme.ShamelaLibraryTheme {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    RequestPermission(permission = Manifest.permission.POST_NOTIFICATIONS, onGranted = {})
                }
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                       HomeHostScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadCompleteReceiver, filter, RECEIVER_EXPORTED)
        }else{
            registerReceiver(downloadCompleteReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(downloadCompleteReceiver)
    }
}

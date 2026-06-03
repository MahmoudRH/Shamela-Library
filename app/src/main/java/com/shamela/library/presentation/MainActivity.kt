package com.shamela.library.presentation

import android.Manifest
import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowInsetsControllerCompat
import com.shamela.apptheme.data.sharedPrefs.SharedPreferencesData
import com.shamela.apptheme.domain.usecases.userPreferences.ReadUserPreferences
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import com.shamela.apptheme.presentation.util.RequestPermission
import com.shamela.library.presentation.navigation.AboutApp
import com.shamela.library.presentation.reciever.DownloadCompleteReceiver
import com.shamela.library.presentation.screens.HomeHostScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val downloadCompleteReceiver = DownloadCompleteReceiver()
    private val userPreferences = SharedPreferencesData(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAboutScreenIntent(intent)

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false

        ReadUserPreferences(userPreferences).invoke().apply {
            AppFonts.changeFontFamily(AppFonts.fontFamilyOf(fontFamily))
            AppFonts.changeFontSize(fontSize)
            AppTheme.changeColorScheme(
                AppTheme.themeOf(
                    theme,
                    colorSchemeName,
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAboutScreenIntent(intent)
    }

    private fun handleAboutScreenIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_ABOUT_SCREEN, false) == true) {
            AboutApp.requestOpen()
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

    companion object {
        const val EXTRA_OPEN_ABOUT_SCREEN = "OPEN_ABOUT_SCREEN"
    }
}

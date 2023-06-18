package com.shamela.library.presentation

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
    val userPreferences = SharedPreferencesData(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReadUserPreferences(userPreferences).invoke().apply {
            AppFonts.changeFontFamily(AppFonts.fontFamilyOf(fontFamily))
            AppFonts.changeFontSize(fontSize)
            AppTheme.changeColorScheme(
                AppTheme.themeOf(
                    theme,
                    colorScheme,
                    isNightMode(this@MainActivity),
                    this@MainActivity
                ),
                theme
            )
            if (libraryUri == null) {
                showPermissionDialog()
            }
        }
        setContent {
            AppTheme.ShamelaLibraryTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HomeHostScreen()
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            if (uri != null) {
                userPreferences.saveLibraryFolderUri(uri)
            } else {
                // Permission is denied, handle the case where the user denied the permission
                Toast.makeText(this, "الرجاء منح إذن الوصول للذاكرة", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestSAFPermission() {
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val initialUri = Uri.parse(downloadsFolder.absolutePath)
        requestPermissionLauncher.launch(initialUri)
    }
    private fun showPermissionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("اذن الوصول للملفات")
            .setMessage("التطبيق بحاجة لإذن الوصول للملفات حتى يعمل بشكل صحيح")
            .setPositiveButton("منح الإذن") { _, _ ->
                requestSAFPermission()
            }
            .create()
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadCompleteReceiver, filter)
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

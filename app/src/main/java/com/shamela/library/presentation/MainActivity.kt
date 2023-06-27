package com.shamela.library.presentation

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.folioreader.Constants
import com.shamela.apptheme.theme.AppFonts
import com.shamela.apptheme.theme.AppTheme
import com.shamela.library.data.local.sharedPrefs.SharedPreferencesData
import com.shamela.library.domain.usecases.userPreferences.ReadUserPreferences
import com.shamela.library.presentation.reciever.DownloadCompleteReceiver
import com.shamela.library.presentation.screens.HomeHostScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val downloadCompleteReceiver = DownloadCompleteReceiver()
    val userPreferences = SharedPreferencesData(this)
    private val permissionState = mutableStateOf(false)
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
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                   if( isPermissionGranted() || permissionState.value) {
                       HomeHostScreen()
                   }else{
                       showPermissionDialog()
                   }
                }
            }
        }
    }
    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11(R) or above
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11(R) or above
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            //Android is below 11(R)
            ActivityCompat.requestPermissions(
                this@MainActivity,
                Constants.writeExternalStoragePerms,
                Constants.WRITE_EXTERNAL_STORAGE_REQUEST
            )
        }
    }

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            //here we will handle the result of our intent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(
                        this,
                        "تم منح إذن الوصول للملفات....",
                        Toast.LENGTH_SHORT
                    ).show()
                    permissionState.value = true
                    //Do Whatever you want
                } else {
                    //Manage External Storage Permission is denied....
                    Toast.makeText(
                        this,
                        "تم رفض إذن الوصول للملفات....",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    private fun showPermissionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("اذن الوصول للملفات")
            .setMessage("التطبيق بحاجة لإذن الوصول للملفات حتى يعمل بشكل صحيح")
            .setPositiveButton("منح الإذن") { _, _ ->
                requestPermission()
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
}

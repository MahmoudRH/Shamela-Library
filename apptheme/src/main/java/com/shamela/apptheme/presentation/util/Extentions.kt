package com.shamela.apptheme.presentation.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.content.ContextCompat

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
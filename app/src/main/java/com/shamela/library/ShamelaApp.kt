package com.shamela.library

import android.app.Application
import com.shamela.apptheme.data.sharedPrefs.SharedPreferencesData
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import com.shamela.apptheme.presentation.theme.colors.AppColors
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class ShamelaApp : Application() {
    companion object {
        lateinit var externalMediaDir: File
            private set
    }

    override fun onCreate() {
        super.onCreate()
        externalMediaDir = externalMediaDirs.firstOrNull() ?: run {
            File(applicationContext.filesDir, "fallback_directory")
        }
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
    }
}
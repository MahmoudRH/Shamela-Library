package com.shamela.library

import android.app.Application
import com.shamela.apptheme.theme.AppFonts
import com.shamela.apptheme.theme.AppTheme
import com.shamela.apptheme.theme.colors.AppColors
import com.shamela.library.data.local.sharedPrefs.SharedPreferencesData
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShamelaApp : Application() {

    override fun onCreate() {
        super.onCreate()
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
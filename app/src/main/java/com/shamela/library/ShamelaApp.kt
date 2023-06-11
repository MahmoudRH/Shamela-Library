package com.shamela.library

import android.app.Application
import com.shamela.library.data.local.sharedPrefs.SharedPreferencesData
import com.shamela.library.presentaion.theme.colors.AppColors
import com.shamela.library.presentaion.theme.AppFonts
import com.shamela.library.presentaion.theme.AppTheme
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
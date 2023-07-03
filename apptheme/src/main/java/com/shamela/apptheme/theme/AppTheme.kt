package com.shamela.apptheme.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.shamela.apptheme.theme.colors.AppColors


object AppTheme {
    private const val DEFAULT = "تلقائي"
    private const val LIGHT = "فاتح"
    private const val DARK = "مظلم"


    private val selectedColorScheme = mutableStateOf(lightColorScheme())
    private val preferredTheme = mutableStateOf(DEFAULT)

    fun changeColorScheme(newColorScheme: ColorScheme, newPreferredTheme: String) {
        selectedColorScheme.value = newColorScheme
        preferredTheme.value = newPreferredTheme
    }

    val colorScheme by derivedStateOf {
        selectedColorScheme.value
    }

    private val theme by derivedStateOf {
        preferredTheme.value
    }

    private val availableThemes = setOf(DEFAULT, LIGHT, DARK)
    fun getAvailableThemes(): Set<String> = availableThemes

    fun themeOf(
        theme: String,
        colorScheme: String,
        isSystemInDarkTheme: Boolean,
        context: Context,
    ): ColorScheme {
        val darkTheme = when (theme) {
            LIGHT -> false
            DARK -> true
            DEFAULT -> isSystemInDarkTheme
            else -> false
        }

        return AppColors.colorSchemeOf(colorScheme, context).run {
            if (darkTheme) darkColorScheme else lightColorScheme
        }
    }

    fun isDarkTheme(context: Context): Boolean {
        val nightModeFlags =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemInDarkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        return (theme == DEFAULT && isSystemInDarkTheme) || theme == DARK
    }

    @Composable
    fun ShamelaLibraryTheme(
        content: @Composable () -> Unit,
    ) {
        val statusBar = when (theme) {
            DEFAULT -> isSystemInDarkTheme()
            DARK -> true
            else -> false
        }
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    statusBar
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppFonts.Typography,
            content = content
        )
    }
}


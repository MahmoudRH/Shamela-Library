package com.shamela.apptheme.presentation.settings

import androidx.compose.material3.ColorScheme
import com.shamela.apptheme.domain.model.UserPrefs


sealed class SettingsEvent {
    class OnChangeAppFont(val newPrefs: UserPrefs) : SettingsEvent()
    class OnChangeAppFontSize(val newPrefs: UserPrefs) : SettingsEvent()
    class OnChangeSliderPosition(val newPosition: Float) : SettingsEvent()
    class OnChangeAppTheme(val colorScheme: ColorScheme, val userPrefs: UserPrefs) : SettingsEvent()
}
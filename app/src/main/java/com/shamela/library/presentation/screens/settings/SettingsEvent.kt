package com.shamela.library.presentation.screens.settings

import androidx.compose.material3.ColorScheme
import com.shamela.library.domain.model.UserPrefs


sealed class SettingsEvent {
    class OnChangeAppFont(val newPrefs: UserPrefs) : SettingsEvent()
    class OnChangeAppFontSize(val newPrefs: UserPrefs) : SettingsEvent()
    class OnChangeSliderPosition(val newPosition: Float) : SettingsEvent()
    class OnChangeAppTheme(val colorScheme: ColorScheme, val userPrefs: UserPrefs) : SettingsEvent()
}
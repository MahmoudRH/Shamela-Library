package com.shamela.apptheme.presentation.settings

import androidx.compose.material3.ColorScheme
import com.shamela.apptheme.domain.model.UserPrefs


sealed class PreferenceSettingsEvent {
    class OnChangeAppFont(val newPrefs: UserPrefs) : PreferenceSettingsEvent()
    class OnChangeAppFontSize(val newPrefs: UserPrefs) : PreferenceSettingsEvent()
    class OnChangeSliderPosition(val newPosition: Float) : PreferenceSettingsEvent()
    class OnChangeAppTheme(val colorScheme: ColorScheme, val userPrefs: UserPrefs) : PreferenceSettingsEvent()
}
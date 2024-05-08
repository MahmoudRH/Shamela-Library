package com.shamela.apptheme.presentation.settings

import com.shamela.apptheme.domain.model.UserPrefs


data class PreferenceSettingsState(
    val availableThemes: List<String> = emptyList(),
    val availableColorSchemes: List<String> = emptyList(),
    val availableFontFamilies: List<String> = emptyList(),
    val availableFontSizes: List<Int> = emptyList(),
    val userPrefs: UserPrefs = UserPrefs(),
    val sliderPosition:Float = 0f

    )
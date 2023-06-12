package com.shamela.library.presentation.screens.settings

import com.shamela.library.domain.model.UserPrefs


data class SettingsState(
    val availableThemes: List<String> = emptyList(),
    val availableColorSchemes: List<String> = emptyList(),
    val availableFontFamilies: List<String> = emptyList(),
    val availableFontSizes: List<Int> = emptyList(),
    val userPrefs: UserPrefs = UserPrefs(),
    val sliderPosition:Float = 0f

    )
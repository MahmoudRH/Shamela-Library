package com.shamela.library.domain.usecases.userPreferences

data class UserPreferencesUseCases(
    val readUserPreferences: ReadUserPreferences,
    val updateUserPreferences: UpdateUserPreferences,
    val getAvailableFontFamilies: GetAvailableFontFamilies,
    val getAvailableFontSizes: GetAvailableFontSizes,
    val getAvailableThemes: GetAvailableThemes,
    val getAvailableColorSchemes: GetAvailableColorSchemes
)

package com.shamela.library.presentation.screens.settings


import androidx.lifecycle.ViewModel
import com.shamela.library.domain.usecases.userPreferences.UserPreferencesUseCases
import com.shamela.apptheme.theme.AppFonts
import com.shamela.apptheme.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val userPreferencesUseCases: UserPreferencesUseCases) :
    ViewModel() {
    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    init {
        userPreferencesUseCases.getAvailableFontFamilies().let { fonts ->
            _settingsState.update { it.copy(availableFontFamilies = fonts) }
        }
        userPreferencesUseCases.getAvailableFontSizes().let { sizes ->
            _settingsState.update { it.copy(availableFontSizes = sizes.map { v-> v.toInt() }.sorted()) }
        }
        userPreferencesUseCases.getAvailableThemes().let { themes ->
            _settingsState.update { it.copy(availableThemes = themes) }
        }
        userPreferencesUseCases.getAvailableColorSchemes().let { colors ->
            _settingsState.update { it.copy(availableColorSchemes = colors) }
        }
        userPreferencesUseCases.readUserPreferences().let { userPrefs ->
            _settingsState.update { it.copy(userPrefs = userPrefs) }
            val selectedThemePosition = settingsState.value.availableFontSizes.indexOf(userPrefs.fontSize)
            _settingsState.update { it.copy(sliderPosition = selectedThemePosition.toFloat()) }

        }
    }


    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnChangeAppFont -> {
                _settingsState.update { it.copy(userPrefs = event.newPrefs) }
                userPreferencesUseCases.updateUserPreferences(event.newPrefs)
                AppFonts.changeFontFamily(AppFonts.fontFamilyOf(event.newPrefs.fontFamily))
            }

            is SettingsEvent.OnChangeAppTheme -> {
                _settingsState.update { it.copy(userPrefs = event.userPrefs) }
                userPreferencesUseCases.updateUserPreferences(event.userPrefs)
                AppTheme.changeColorScheme(event.colorScheme, event.userPrefs.theme)
            }

            is SettingsEvent.OnChangeAppFontSize -> {
                _settingsState.update { it.copy(userPrefs = event.newPrefs) }
                userPreferencesUseCases.updateUserPreferences(event.newPrefs)
                AppFonts.changeFontSize(event.newPrefs.fontSize)
            }

            is SettingsEvent.OnChangeSliderPosition -> {
                _settingsState.update { it.copy(sliderPosition = event.newPosition) }
            }
        }
    }

}
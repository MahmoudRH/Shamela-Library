package com.shamela.apptheme.presentation.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.shamela.apptheme.data.sharedPrefs.SharedPreferencesData
import com.shamela.apptheme.domain.usecases.userPreferences.GetAvailableColorSchemes
import com.shamela.apptheme.domain.usecases.userPreferences.GetAvailableFontFamilies
import com.shamela.apptheme.domain.usecases.userPreferences.GetAvailableFontSizes
import com.shamela.apptheme.domain.usecases.userPreferences.GetAvailableThemes
import com.shamela.apptheme.domain.usecases.userPreferences.ReadUserPreferences
import com.shamela.apptheme.domain.usecases.userPreferences.UpdateUserPreferences
import com.shamela.apptheme.domain.usecases.userPreferences.UserPreferencesUseCases
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SettingsViewModel(private val userPreferencesUseCases: UserPreferencesUseCases) :
    ViewModel() {
    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    init {
        userPreferencesUseCases.getAvailableFontFamilies().let { fonts ->
            _settingsState.update { it.copy(availableFontFamilies = fonts) }
        }
        userPreferencesUseCases.getAvailableFontSizes().let { sizes ->
            _settingsState.update {
                it.copy(availableFontSizes = sizes.map { v -> v.toInt() }.sorted())
            }
        }
        userPreferencesUseCases.getAvailableThemes().let { themes ->
            _settingsState.update { it.copy(availableThemes = themes) }
        }
        userPreferencesUseCases.getAvailableColorSchemes().let { colors ->
            _settingsState.update { it.copy(availableColorSchemes = colors) }
        }
        userPreferencesUseCases.readUserPreferences().let { userPrefs ->
            _settingsState.update { it.copy(userPrefs = userPrefs) }
            val selectedThemePosition =
                settingsState.value.availableFontSizes.indexOf(userPrefs.fontSize)
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


    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras,
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val dataSource = SharedPreferencesData(application.applicationContext)
                return SettingsViewModel(
                    UserPreferencesUseCases(
                        readUserPreferences = ReadUserPreferences(datasource = dataSource),
                        updateUserPreferences = UpdateUserPreferences(datasource = dataSource),
                        getAvailableFontFamilies = GetAvailableFontFamilies(datasource = dataSource),
                        getAvailableFontSizes = GetAvailableFontSizes(datasource = dataSource),
                        getAvailableThemes = GetAvailableThemes(datasource = dataSource),
                        getAvailableColorSchemes = GetAvailableColorSchemes(datasource = dataSource)
                    )
                ) as T
            }
        }
    }

}
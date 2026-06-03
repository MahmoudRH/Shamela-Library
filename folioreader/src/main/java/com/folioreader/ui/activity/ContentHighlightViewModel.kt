package com.folioreader.ui.activity


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
import com.shamela.apptheme.presentation.settings.PreferenceSettingsEvent
import com.shamela.apptheme.presentation.settings.PreferenceSettingsState
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ContentHighlightViewModel(private val userPreferencesUseCases: UserPreferencesUseCases) :
    ViewModel() {
    private val _preferenceSettings = MutableStateFlow<PreferenceSettingsState>(PreferenceSettingsState())
    val preferenceSettings = _preferenceSettings.asStateFlow()

    init {
        initializeSettingsOptions()
        refreshPreferences()
    }

    fun refreshPreferences() {
        userPreferencesUseCases.readUserPreferences().let { userPrefs ->
            _preferenceSettings.update { it.copy(userPrefs = userPrefs) }
            val selectedThemePosition =
                preferenceSettings.value.availableFontSizes.indexOf(userPrefs.fontSize)
            _preferenceSettings.update { it.copy(sliderPosition = selectedThemePosition.toFloat()) }
        }
    }

    private fun initializeSettingsOptions() {
        userPreferencesUseCases.getAvailableFontFamilies().let { fonts ->
            _preferenceSettings.update { it.copy(availableFontFamilies = fonts) }
        }
        userPreferencesUseCases.getAvailableFontSizes().let { sizes ->
            _preferenceSettings.update {
                it.copy(availableFontSizes = sizes.map { v -> v.toInt() }.sorted())
            }
        }
        userPreferencesUseCases.getAvailableThemes().let { themes ->
            _preferenceSettings.update { it.copy(availableThemes = themes) }
        }
        userPreferencesUseCases.getAvailableColorSchemes().let { colors ->
            _preferenceSettings.update { it.copy(availableColorSchemes = colors) }
        }
    }


    fun onPrefsEvent(event: PreferenceSettingsEvent) {
        when (event) {
            is PreferenceSettingsEvent.OnChangeAppFont -> {
                _preferenceSettings.update { it.copy(userPrefs = event.newPrefs) }
                userPreferencesUseCases.updateUserPreferences(event.newPrefs)
                AppFonts.changeFontFamily(AppFonts.fontFamilyOf(event.newPrefs.fontFamily))
            }

            is PreferenceSettingsEvent.OnChangeAppTheme -> {
                _preferenceSettings.update { it.copy(userPrefs = event.userPrefs) }
                userPreferencesUseCases.updateUserPreferences(event.userPrefs)
                AppTheme.changeColorScheme(event.colorScheme, event.userPrefs.theme)
            }

            is PreferenceSettingsEvent.OnChangeAppFontSize -> {
                _preferenceSettings.update { it.copy(userPrefs = event.newPrefs) }
                userPreferencesUseCases.updateUserPreferences(event.newPrefs)
                AppFonts.changeFontSize(event.newPrefs.fontSize)
            }

            is PreferenceSettingsEvent.OnChangeSliderPosition -> {
                _preferenceSettings.update { it.copy(sliderPosition = event.newPosition) }
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
                return ContentHighlightViewModel(
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
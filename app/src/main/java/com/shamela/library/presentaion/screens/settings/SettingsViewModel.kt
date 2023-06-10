package com.shamela.library.presentaion.screens.settings


import androidx.lifecycle.ViewModel
import com.shamela.library.presentaion.screens.settings.SettingsEvent
import com.shamela.library.presentaion.screens.settings.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor():ViewModel() {
    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState())
    val settingsState = _settingsState.asStateFlow()


    fun onEvent(event: SettingsEvent) {
      when (event) {
          is SettingsEvent.SampleEvent -> {
             _settingsState.update { it.copy(example = event.newText) }
           }
        }
   }

}
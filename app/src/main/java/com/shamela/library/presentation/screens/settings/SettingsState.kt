package com.shamela.library.presentation.screens.settings

import android.net.Uri


data class SettingsState(
    val selectedViewType: SettingsViewType = SettingsViewType.Preferences,
    val isLoading: Boolean = false,
    val fileUri:Uri? = null ,
    val fileName:String = "اختر كتابا",
//    val addStatus: Boolean? = null,
)
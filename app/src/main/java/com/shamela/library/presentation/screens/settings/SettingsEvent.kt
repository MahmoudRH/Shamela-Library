package com.shamela.library.presentation.screens.settings

import android.net.Uri


sealed class SettingsEvent {
    class OnChangeViewType(val newViewType: SettingsViewType) : SettingsEvent()
    class AddExternalBookToLibrary(
        val bookUri: Uri,
        val bookTitle: String,
    ) : SettingsEvent()

    class NewFileSelected(val fileUri: Uri) : SettingsEvent()
}
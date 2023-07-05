package com.folioreader.ui.activity.folioActivity

import android.content.Context

sealed class FolioActivityEvent {
    class InitializeEpubBook(
        val filePath: String,
        val portNumber: Int,
        val context: Context,
        val fontSizeCssClass: String,
        val fontFamilyCssClass: String,
        val isNightMode: Boolean,
    ) : FolioActivityEvent()

    class OnChangeTopActivity(val isTopActivity: Boolean) : FolioActivityEvent()
    class OnChangeTaskImportance(val taskImportance: Int) : FolioActivityEvent()
    class OnChangeSelectedPage(val newPage: Int) : FolioActivityEvent()
    class OnCurrentPageTextChanged(val newPage: String) : FolioActivityEvent()
    object StopStreamerServer : FolioActivityEvent()
    object ToggleAppBarsVisibility : FolioActivityEvent()
    object ToggleMenuVisibility : FolioActivityEvent()
    object DismissMenu : FolioActivityEvent()
}
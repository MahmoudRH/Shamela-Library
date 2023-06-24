package com.folioreader.ui.activity.folioActivity

sealed class FolioActivityEvent {
    class InitializeEpubBook(val filePath: String, val portNumber: Int) : FolioActivityEvent()
    class OnChangeTopActivity(val isTopActivity: Boolean) : FolioActivityEvent()
    class OnChangeTaskImportance(val taskImportance: Int) : FolioActivityEvent()
    class OnChangeSelectedPage(val newPage: Int) : FolioActivityEvent()
    object StopStreamerServer: FolioActivityEvent()
}
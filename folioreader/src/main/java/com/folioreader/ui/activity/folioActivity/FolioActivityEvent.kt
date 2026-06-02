package com.folioreader.ui.activity.folioActivity


sealed class FolioActivityEvent {

    class InitializeBook(val filePath: String) : FolioActivityEvent()
    class OnChangeTaskImportance(val taskImportance: Int) : FolioActivityEvent()
    class OnChangeTopActivity(val isTopActivity: Boolean) : FolioActivityEvent()
    class OnSearchResult(val href: String, val jsCall: String) : FolioActivityEvent()
    class OnSelectedChapter(val href: String) : FolioActivityEvent()
    class OnSettingsChanged(val hash: Int) : FolioActivityEvent()
    object StopStreamerServer : FolioActivityEvent()

}
package com.folioreader.ui.activity.folioActivity


sealed class FolioActivityEvent {

    class InitializeBook(val filePath:String) : FolioActivityEvent()
    class OnChangeTaskImportance(val taskImportance: Int) : FolioActivityEvent()
    class OnChangeTopActivity(val isTopActivity: Boolean) : FolioActivityEvent()
    object StopStreamerServer : FolioActivityEvent()



}
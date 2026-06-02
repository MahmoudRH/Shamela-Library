package com.shamela.library.presentation.screens.about

sealed class AboutAppEvent {
    object FetchLatestRelease : AboutAppEvent()
    object DownloadAndInstall : AboutAppEvent()
}

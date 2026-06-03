package com.shamela.library.presentation.screens.about

data class AboutAppState(
    val currentVersion: String = "",
    val latestVersion: String = "",
    val releaseNotes: String = "",
    val isLoadingLatestVersion: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val error: String? = null,
)

val AboutAppState.updateAvailable: Boolean
    get() = latestVersion.isNotEmpty() && latestVersion != currentVersion

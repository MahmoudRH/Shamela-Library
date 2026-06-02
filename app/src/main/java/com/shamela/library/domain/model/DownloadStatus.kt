package com.shamela.library.domain.model

sealed class DownloadStatus {
    object NotDownloaded : DownloadStatus()
    data class Downloading(val progress: Int) : DownloadStatus()
    object Downloaded : DownloadStatus()
}

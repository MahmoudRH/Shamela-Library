package com.shamela.library.presentation.screens.download

import com.shamela.library.presentation.screens.library.ViewType


sealed class DownloadEvent{
    class OnChangeViewType(val newViewType: ViewType): DownloadEvent()
    object LoadUserBooksAndSections: DownloadEvent()}
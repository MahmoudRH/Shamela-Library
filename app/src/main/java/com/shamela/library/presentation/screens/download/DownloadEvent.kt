package com.shamela.library.presentation.screens.download

import com.shamela.library.domain.model.Book
import com.shamela.library.presentation.screens.library.ViewType


sealed class DownloadEvent{
    class OnChangeViewType(val newViewType: ViewType): DownloadEvent()
    object LoadUserBooks: DownloadEvent()
    object LoadUserSections: DownloadEvent()
    class OnClickDownloadBook(val book: Book):DownloadEvent()

}
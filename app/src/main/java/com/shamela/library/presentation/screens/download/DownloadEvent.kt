package com.shamela.library.presentation.screens.download

import com.shamela.library.domain.model.Book
import com.shamela.library.presentation.screens.library.BooksViewType


sealed class DownloadEvent{
    class OnChangeViewType(val newBooksViewType: BooksViewType): DownloadEvent()
    object LoadUserBooks: DownloadEvent()
    object LoadUserSections: DownloadEvent()
    class OnClickDownloadBook(val book: Book):DownloadEvent()

}
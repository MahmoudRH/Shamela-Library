package com.shamela.library.presentation.screens.sectionBooks

import com.shamela.library.domain.model.Book


sealed class SectionBooksEvent {
    object LoadBooks : SectionBooksEvent()
    class OnClickDownloadBook(val book: Book) : SectionBooksEvent()
}
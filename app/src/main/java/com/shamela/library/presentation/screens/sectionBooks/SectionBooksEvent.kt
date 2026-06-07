package com.shamela.library.presentation.screens.sectionBooks

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Quote
import com.shamela.library.domain.util.BookSortOption


sealed class SectionBooksEvent {
    object LoadBooks : SectionBooksEvent()
    class OnClickDownloadBook(val book: Book) : SectionBooksEvent()
    object OnClickDownloadSection : SectionBooksEvent()
    class OnClickCancelDownload(val bookId: String) : SectionBooksEvent()
    class AddQuoteToFavorite(val quote: Quote) : SectionBooksEvent()
    class OnChangeSortOption(val option: BookSortOption) : SectionBooksEvent()
    object OnToggleSortDirection : SectionBooksEvent()
}
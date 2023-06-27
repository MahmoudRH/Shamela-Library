package com.shamela.library.presentation.screens.searchResults

import com.shamela.library.domain.model.Book
import com.shamela.library.presentation.screens.sectionBooks.SectionBooksEvent

sealed class SearchResultsEvent{
    class Search(val query: String):SearchResultsEvent()
    class OnSearchQueryChanged(val newSearchQuery: String) : SearchResultsEvent()
    object ClearSearchQuery: SearchResultsEvent()
    class OnClickDownloadBook(val book: Book) : SearchResultsEvent()
}
package com.shamela.library.presentation.screens.searchResults

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Quote

sealed class SearchResultsEvent {
    class Search(val query: String) : SearchResultsEvent()
    class OnSearchQueryChanged(val newSearchQuery: String) : SearchResultsEvent()
    object ClearSearchQuery : SearchResultsEvent()
    class OnClickDownloadBook(val book: Book) : SearchResultsEvent()
    class AddQuoteToFavorite(val quote: Quote) : SearchResultsEvent()
}
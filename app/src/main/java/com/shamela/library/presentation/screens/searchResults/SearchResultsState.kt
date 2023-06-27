package com.shamela.library.presentation.screens.searchResults

import com.shamela.library.domain.model.Book


data class SearchResultsState(
    val type: String = "",
    val query: String = "",
    val lastQuery: String = "",
    val resultsList: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val isListEmpty: Boolean = false,
)
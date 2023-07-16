package com.shamela.library.presentation.screens.searchResults

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category


data class SearchResultsState(
    val type: String = "", //local or remote
    val query: String = "",
    val lastQuery: String = "",
    val booksResultsList: List<Book> = emptyList(),
    val sectionsResultsList: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isListEmpty: Boolean = false,
)
package com.shamela.library.presentation.screens.searchResults

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.model.DownloadStatus


data class SearchResultsState(
    val type: String = "", //local or remote
    val query: String = "",
    val lastQuery: String = "",
    val booksResultsList: List<Book> = emptyList(),
    val sectionsResultsList: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isListEmpty: Boolean = false,
    val downloadStatuses: Map<String, DownloadStatus> = emptyMap(),
    val downloadedBookIds: Set<String> = emptySet(),
)
package com.shamela.library.presentation.screens.sectionBooks

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.DownloadStatus
import com.shamela.library.domain.util.BookSortOption


data class SectionBooksState(
    val books: Map<String, Book> = emptyMap(),
    val type: String = "",
    val isLoading: Boolean = true,
    val isDownloadButtonEnabled: Boolean = true,
    val downloadStatuses: Map<String, DownloadStatus> = emptyMap(),
    val downloadedBookIds: Set<String> = emptySet(),
    val sortOption: BookSortOption = BookSortOption.NAME,
    val sortAscending: Boolean = BookSortOption.NAME.defaultAscending,
    /** Book id -> download time (file last-modified), filled lazily for local sections only. */
    val downloadTimes: Map<String, Long> = emptyMap(),
)
package com.shamela.library.presentation.screens.sectionBooks

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.DownloadStatus


data class SectionBooksState(
    val books: Map<String, Book> = emptyMap(),
    val type: String = "",
    val isLoading: Boolean = true,
    val isDownloadButtonEnabled: Boolean = true,
    val downloadStatuses: Map<String, DownloadStatus> = emptyMap(),
    val downloadedBookIds: Set<String> = emptySet(),
)
package com.shamela.library.presentation.screens.download

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.model.DownloadStatus
import com.shamela.library.presentation.screens.library.BooksViewType


data class DownloadState(
    val booksViewType: BooksViewType = BooksViewType.Sections,
    val books: List<Book> = emptyList(),
    val groupedBooks: Map<Char, List<Book>> = emptyMap(),
    val sections: List<Category> = emptyList(),
    val isLoadingBooks: Boolean = false,
    val downloadStatuses: Map<String, DownloadStatus> = emptyMap(),
    val downloadedBookIds: Set<String> = emptySet(),
)
package com.shamela.library.presentation.screens.library

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.util.BookSortOption


data class LibraryState(
    val booksViewType: BooksViewType = BooksViewType.Books,
    val isLoading: Boolean = true,
    val books: Map<String, Book> = emptyMap(),
    val sections: Map<String,Category> = emptyMap(),
    val selectedBooks : List<Book> = emptyList(),
    val sortOption: BookSortOption = BookSortOption.NAME,
    val sortAscending: Boolean = BookSortOption.NAME.defaultAscending,
    /** Book id -> download time (file last-modified), filled lazily when sorting by download time. */
    val downloadTimes: Map<String, Long> = emptyMap(),
)
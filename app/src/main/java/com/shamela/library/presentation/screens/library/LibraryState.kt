package com.shamela.library.presentation.screens.library

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category


data class LibraryState(
    val booksViewType: BooksViewType = BooksViewType.Books,
    val isLoading: Boolean = true,
    val books: Map<String, Book> = emptyMap(),
    val sections: Map<String,Category> = emptyMap(),
)
package com.shamela.library.presentation.screens.sectionBooks

import com.shamela.library.domain.model.Book


data class SectionBooksState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true,
)
package com.shamela.library.presentation.screens.sectionBooks

import com.shamela.library.domain.model.Book


data class SectionBooksState(
    val books: Map<String, Book> = emptyMap(),
    val type: String = "",
    val isLoading: Boolean = true,
    val isDownloadButtonEnabled:Boolean = true,
)
package com.shamela.library.presentation.screens.library

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category


data class LibraryState(
    val viewType: ViewType = ViewType.Books,
    val isLoading:Boolean = true,
    val books:List<Book> = emptyList(),
    val sections:List<Category> = emptyList(),
)
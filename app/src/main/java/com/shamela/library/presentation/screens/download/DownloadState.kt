package com.shamela.library.presentation.screens.download

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.presentation.screens.library.BooksViewType


data class DownloadState(
    val booksViewType: BooksViewType = BooksViewType.Sections,
    val books:List<Book> = emptyList(),
    val sections:List<Category> = emptyList(),
    val isLoading:Boolean = false,
)
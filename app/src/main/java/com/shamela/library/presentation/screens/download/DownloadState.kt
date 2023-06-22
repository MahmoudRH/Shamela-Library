package com.shamela.library.presentation.screens.download

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.presentation.screens.library.ViewType


data class DownloadState(
    val viewType: ViewType = ViewType.Sections,
    val isLoading:Boolean = true,
    val books:List<Book> = emptyList(),
    val sections:List<Category> = emptyList(),
)
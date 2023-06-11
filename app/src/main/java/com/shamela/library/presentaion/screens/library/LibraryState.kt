package com.shamela.library.presentaion.screens.library

import com.shamela.library.presentaion.model.Book
import com.shamela.library.presentaion.model.Section


data class LibraryState(
    val viewType: ViewType = ViewType.Books,
    val isLoading:Boolean = true,
    val books:List<Book> = emptyList(),
    val sections:List<Section> = emptyList(),
)
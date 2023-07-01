package com.shamela.library.presentation.screens.favorite

import com.shamela.library.domain.model.Book


data class FavoriteState(
    val favoriteBooks: List<Book> = emptyList(),
    val isListEmpty: Boolean = false,

    )
package com.shamela.library.presentation.screens.favorite

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Quote


data class FavoriteState(
    val favoriteBooks: List<Book> = emptyList(),
    val favoriteQuotes: List<Quote> = emptyList(),
    val isListEmpty: Boolean = false,
    val viewType: FavoriteViewType = FavoriteViewType.Quotes,
)
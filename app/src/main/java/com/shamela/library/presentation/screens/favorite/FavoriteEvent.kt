package com.shamela.library.presentation.screens.favorite

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Quote


sealed class FavoriteEvent {
    object LoadFavoriteBooks : FavoriteEvent()
    object LoadFavoriteQuotes : FavoriteEvent()
    class ToggleFavorite(val book: Book) : FavoriteEvent()
    class AddQuoteToFavorite(val quote: Quote) : FavoriteEvent()

    class OnChangeViewType(val newViewType: FavoriteViewType) : FavoriteEvent()
    class OpenBookForQuote(val currentQuote: Quote) : FavoriteEvent()

}
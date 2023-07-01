package com.shamela.library.presentation.screens.favorite

import com.shamela.library.domain.model.Book


sealed class FavoriteEvent {
    object LoadFavoriteBooks : FavoriteEvent()
    class ToggleFavorite(val book: Book) : FavoriteEvent()

}
package com.shamela.library.presentation.screens.library

import com.shamela.library.domain.model.Book


sealed class LibraryEvent {
    class OnChangeViewType(val newViewType: ViewType) : LibraryEvent()
    class ToggleFavorite(val book: Book) : LibraryEvent()

    object LoadUserBooksAndSections : LibraryEvent()

}
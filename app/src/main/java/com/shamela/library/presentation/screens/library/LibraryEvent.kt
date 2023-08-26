package com.shamela.library.presentation.screens.library

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Quote


sealed class LibraryEvent {
    class OnChangeViewType(val newBooksViewType: BooksViewType) : LibraryEvent()
    class ToggleFavorite(val book: Book) : LibraryEvent()
    class AddQuoteToFavorite(val quote: Quote) : LibraryEvent()
    class DeleteBook(val book: Book) : LibraryEvent()
    object LoadUserBooksAndSections : LibraryEvent()
    class SelectBook(val book: Book) : LibraryEvent()
    object DeleteSelectedBooks:LibraryEvent()
    object CancelSelection:LibraryEvent()

}
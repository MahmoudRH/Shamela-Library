package com.shamela.library.presentation.screens.library


sealed class LibraryEvent{
    class OnChangeViewType(val newViewType: ViewType):LibraryEvent()
    object LoadUserBooksAndSections:LibraryEvent()

}
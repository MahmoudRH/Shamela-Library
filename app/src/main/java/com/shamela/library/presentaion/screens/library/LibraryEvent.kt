package com.shamela.library.presentaion.screens.library


sealed class LibraryEvent{
    class OnChangeViewType(val newViewType: ViewType):LibraryEvent()
    object LoadUserBooksAndSections:LibraryEvent()
}
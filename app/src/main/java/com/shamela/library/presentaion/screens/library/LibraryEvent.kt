package com.shamela.library.presentaion.screens.library


sealed class LibraryEvent{
    class SampleEvent(val newText: String):LibraryEvent()
}
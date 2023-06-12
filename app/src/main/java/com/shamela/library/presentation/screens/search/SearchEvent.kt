package com.shamela.library.presentation.screens.search


sealed class SearchEvent{
    class SampleEvent(val newText: String):SearchEvent()
}
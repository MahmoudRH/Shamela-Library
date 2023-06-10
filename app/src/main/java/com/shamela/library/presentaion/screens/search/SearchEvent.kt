package com.shamela.library.presentaion.screens.search


sealed class SearchEvent{
    class SampleEvent(val newText: String):SearchEvent()
}
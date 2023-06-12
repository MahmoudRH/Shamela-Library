package com.shamela.library.presentation.screens.favorite


sealed class FavoriteEvent{
    class SampleEvent(val newText: String):FavoriteEvent()
}
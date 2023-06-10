package com.shamela.library.presentaion.screens.favorite


sealed class FavoriteEvent{
    class SampleEvent(val newText: String):FavoriteEvent()
}
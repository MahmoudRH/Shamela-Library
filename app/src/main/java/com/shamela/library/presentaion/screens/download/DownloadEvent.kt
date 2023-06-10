package com.shamela.library.presentaion.screens.download


sealed class DownloadEvent{
    class SampleEvent(val newText: String):DownloadEvent()
}
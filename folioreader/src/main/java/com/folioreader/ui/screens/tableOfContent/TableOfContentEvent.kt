package com.folioreader.ui.screens.tableOfContent


sealed class TableOfContentEvent{
    class SampleEvent(val newText: String):TableOfContentEvent()
}
package com.folioreader.ui.screens.highlights

import com.folioreader.model.HighlightImpl


sealed class HighlightsEvent{
    class GetHighlightsOf(val bookId: String):HighlightsEvent()
    class DeleteItem(val item: HighlightImpl):HighlightsEvent()
}
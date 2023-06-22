package com.folioreader.ui.screens.highlights

import com.folioreader.model.HighlightImpl


data class HighlightsState(
    val isLoading: Boolean = true,
    val highlightsList: List<HighlightImpl> = emptyList()
)
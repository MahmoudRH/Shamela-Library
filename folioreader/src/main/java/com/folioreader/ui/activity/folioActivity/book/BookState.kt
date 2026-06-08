package com.folioreader.ui.activity.folioActivity.book

import com.shamela.apptheme.domain.model.DictionaryEntry


data class BookState(
    val isLoading: Boolean = true,
    val mimeType: String = "text/html",
    val pagesMap: Map<Int, Pair<String, String>> = emptyMap(), //Map of pageIndex->(pageUrl to pageHtmlContent)
    val isAppBarsVisible: Boolean = true,
    val isMenuVisible: Boolean = false,
    val currentPageText: String = "0",
    // Term-clarification (offline dictionary) sheet state
    val showMeaningSheet: Boolean = false,
    val meaningQuery: String = "",
    val meaningLoading: Boolean = false,
    val meaningResults: List<DictionaryEntry> = emptyList(),
    val dictionaryAvailable: Boolean = true,
)
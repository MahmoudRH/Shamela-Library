package com.folioreader.ui.activity.folioActivity.book


data class BookState(
    val isLoading: Boolean = true,
    val mimeType: String = "",
    val pagesMap: Map<Int, Pair<String, String>> = emptyMap(), //Map of pageIndex->(pageUrl to pageHtmlContent)
    val isAppBarsVisible: Boolean = true,
    val isMenuVisible: Boolean = false,
    val currentPageText: String = "0",
)
package com.folioreader.ui.activity.folioActivity

import org.readium.r2.shared.Publication

data class FolioActivityState(
    val isLoading: Boolean = true,
    val isTopActivity: Boolean = true,
    val taskImportance: Int = 0,
    val bookTitle: String = "",
    val mimeType: String = "",
    val pagesMap: Map<Int,Pair<String,String>> = emptyMap(), //Map of pageIndex->(pageUrl to pageHtmlContent)
    val streamUrl: String = "",
    val publication: Publication? = null,
    val isAppBarsVisible :Boolean = true,
    val isMenuVisible :Boolean = false,
    val currentPageText:String = "0",
)
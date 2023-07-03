package com.folioreader.ui.activity.folioActivity

import org.readium.r2.shared.Publication

data class FolioActivityState(
    val isLoading: Boolean = true,
    val isPageLoading: Boolean = true,
    val isTopActivity: Boolean = true,
    val taskImportance: Int = 0,
    val bookTitle: String = "",
    val htmlData: List<String> = emptyList(),
    val mimeType: String = "",
    val bookPages: List<String> = emptyList(), //list of urls
    val streamUrl: String = "",
    val selectedPageData: String = "",
    val selectedPageUrl: String = "",
    val publication: Publication? = null,
    val isAppBarsVisible :Boolean = true,
    val isMenuVisible :Boolean = false,
    val currentPageText:String = "0",
)
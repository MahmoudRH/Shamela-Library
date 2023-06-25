package com.folioreader.ui.activity.searchActivity

import org.readium.r2.shared.Locator

data class SearchState(
    val isLoading:Boolean = false,
    val searchProgress:Float = 0f,
    val searchResults: List<Locator> = emptyList(),
    val isListEmpty: Boolean = false,
    val searchQuery:String = ""
)

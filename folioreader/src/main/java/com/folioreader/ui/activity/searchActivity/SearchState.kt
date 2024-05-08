package com.folioreader.ui.activity.searchActivity

import com.folioreader.model.locators.SearchLocator

data class SearchState(
    val isLoading:Boolean = false,
    val searchProgress:Float = 0f,
    val searchResults: List<SearchLocator> = emptyList(),
    val sectionSearchResults: List<Pair<String,SearchLocator>> = emptyList(),
    val isListEmpty: Boolean = false,
    val searchQuery:String = ""
)

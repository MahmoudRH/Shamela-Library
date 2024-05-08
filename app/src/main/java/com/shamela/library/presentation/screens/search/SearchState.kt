package com.shamela.library.presentation.screens.search

import com.folioreader.model.locators.SearchLocator
import com.shamela.library.domain.model.Category


data class SearchState(
    val example:String = "",
    val searchQuery:String = "",
    val allCategories: List<Category> = emptyList(),
    val selectedCategories: List<Category> = emptyList(),
    val isListExpanded:Boolean = false,
    val isLoading:Boolean = false,
    val searchResults:List<SearchLocator> = emptyList()
)


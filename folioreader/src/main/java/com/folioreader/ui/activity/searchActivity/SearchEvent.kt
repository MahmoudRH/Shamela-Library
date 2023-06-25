package com.folioreader.ui.activity.searchActivity

sealed class SearchEvent {
    class Search(val query: String) : SearchEvent()
    class OnSearchQueryChanged(val newSearchQuery: String) : SearchEvent()
    object ClearSearchQuery: SearchEvent()
}

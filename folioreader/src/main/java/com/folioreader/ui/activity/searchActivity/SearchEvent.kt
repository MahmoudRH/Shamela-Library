package com.folioreader.ui.activity.searchActivity

import android.content.Context

sealed class SearchEvent {
    class SearchBook(val bookId: String, val query: String, val context: Context) : SearchEvent()
    class OnSearchQueryChanged(val newSearchQuery: String) : SearchEvent()
    class InitEpub(val epubFilePath: String?) : SearchEvent()
    class SearchCategories(val searchQuery: String,  val searchCategories: List<String>, val context: Context) : SearchEvent()

    object ClearSearchQuery : SearchEvent()
}

package com.folioreader.ui.activity.searchActivity

import android.content.Context

sealed class SearchEvent {
    class Search(val bookId:String ,val query: String, val context: Context) : SearchEvent()
    class OnSearchQueryChanged(val newSearchQuery: String) : SearchEvent()
    class InitEpub(val epubFilePath: String) : SearchEvent()

    object ClearSearchQuery: SearchEvent()
}

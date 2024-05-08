package com.shamela.library.presentation.screens.search

import android.content.Context
import com.shamela.library.domain.model.Category


sealed class SearchEvent {
    class SampleEvent(val newText: String) : SearchEvent()
    class OnChangeSearchQuery(val newText: String) : SearchEvent()
    class ItemChecked(val category: Category) : SearchEvent()
    class DoSearch(val query: String, val selectedCategories: List<String>, val context:Context) : SearchEvent()

    object GetAllCategories : SearchEvent()
    object ToggleCategoriesList : SearchEvent()
    object CloseCategoriesList : SearchEvent()
}
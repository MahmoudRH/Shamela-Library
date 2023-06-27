package com.folioreader.ui.activity.searchActivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioreader.FolioReader
import com.folioreader.model.locators.SearchLocator
import com.folioreader.model.locators.toSearchLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.Locator

class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow<SearchState>(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()
    private val streamerApi = FolioReader.get().r2StreamerApi
    var spineSize = 0
    private var searchJob: Job? = null


    fun onEven(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.newSearchQuery) }
            }

            is SearchEvent.Search -> {
                streamerApi?.let {
                    event.query.trim().let { query ->
                        if (query.isNotBlank()) {
                            _state.update { it.copy(isLoading = true, searchResults = emptyList()) }
                            searchJob?.cancel()

                            searchJob = viewModelScope.launch {
                                withContext(Dispatchers.IO) {
                                    (0 until spineSize).forEach { page ->
                                        val resultsList = it.search(page, query).map { it.toSearchLocator() }
                                        _state.update {
                                            it.copy(
                                                searchResults = it.searchResults + resultsList,
                                                searchProgress = ((page + 1f) / spineSize)
                                            )
                                        }
                                    }
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            isListEmpty = it.searchResults.isEmpty()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SearchEvent.ClearSearchQuery -> {
                _state.update { it.copy(searchQuery = "", isLoading = false) }
                searchJob?.cancel()
            }
        }
    }
}
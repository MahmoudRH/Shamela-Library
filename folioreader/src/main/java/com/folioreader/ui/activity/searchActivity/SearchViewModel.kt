package com.folioreader.ui.activity.searchActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioreader.FolioReader
import com.folioreader.model.locators.toSearchLocator
import com.shamela.apptheme.data.db.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.Publication
import org.readium.r2.streamer.parser.EpubParser

class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow<SearchState>(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()
    private val streamerApi = FolioReader.get().r2StreamerApi
    private var searchJob: Job? = null
    private var publication: Publication? = null
    private var database: DatabaseHelper? = null
    fun onEven(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.newSearchQuery) }
            }

            is SearchEvent.Search -> {
                if (database === null) {
                    database = DatabaseHelper(event.context)
                }
                streamerApi?.let { searchApi ->
                    event.query.trim().let { query ->
                        if (query.isNotBlank()) {
                            _state.update { it.copy(isLoading = true, searchResults = emptyList()) }
                            searchJob?.cancel()

                            searchJob = viewModelScope.launch {
                                withContext(Dispatchers.IO) {
                                    val bookPages = database?.searchBook(event.bookId, event.query)
                                        ?: emptyList()
                                    /*    publication?.let {
                                            var currentPage =0
                                            val searchLocators = bookPages.map { page ->
                                                val queryIndex = page.content.indexOf(query)
                                                val textBefore = page.content.run {
                                                    substring(
                                                        maxOf(queryIndex - 50, 0),
                                                        queryIndex
                                                    )
                                                }
                                                val textAfter = page.content.run {
                                                    substring(
                                                        queryIndex + query.length,
                                                        minOf(queryIndex  + query.length + 50, lastIndex)
                                                    )
                                                }
                                                currentPage++
                                                Locator(
                                                    href = page.href,
                                                    created = 0,
                                                    title = "",
                                                    locations = Locations(cfi = "...$textBefore $query $textAfter..."),
                                                    text = LocatorText(
                                                        before = textBefore,
                                                        hightlight = query,
                                                        after = textAfter
                                                    )
                                                ).toSearchLocator()
                                            }
                                            _state.update {
                                                it.copy(
                                                    searchResults = it.searchResults + searchLocators,
                                                    searchProgress = ((currentPage + 1f) / (publication?.readingOrder?.size
                                                        ?: 1))
                                                )
                                            }
                                        }*/
                                    publication?.let { publication ->
                                        bookPages
                                            .map { page -> publication.readingOrder.indexOfFirst { it.href == page.href } }
                                            .sorted()
                                            .forEach { index ->
                                                val results = searchApi.search(index, query).map { it.toSearchLocator() }
                                                _state.update {
                                                    it.copy(
                                                        searchResults = it.searchResults + results,
                                                        searchProgress = ((index + 1f) / publication.readingOrder.size)
                                                    )
                                                }
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

            is SearchEvent.InitEpub -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        publication = EpubParser().parse(event.epubFilePath)?.publication
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        database?.close()

    }
}
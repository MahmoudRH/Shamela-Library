package com.folioreader.ui.activity.searchActivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioreader.FolioReader
import com.folioreader.model.locators.toSearchLocator
import com.folioreader.util.AppUtil
import com.shamela.apptheme.data.db.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.Locations
import org.readium.r2.shared.Locator
import org.readium.r2.shared.LocatorText
import org.readium.r2.shared.Publication
import org.readium.r2.streamer.parser.EpubParser

class SearchViewModel : ViewModel() {
    private val TAG = "SearchViewModel"
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

            is SearchEvent.SearchBook -> {
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
                                                val results = searchApi.search(index, query)
                                                    .map { it.toSearchLocator() }
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
                        event.epubFilePath?.let {
                            publication = EpubParser().parse(it)?.publication
                        }
                    }
                }
            }

            is SearchEvent.SearchCategories -> {
                Log.e(TAG, "SearchCategories: search started")

                if (database === null) {
                    database = DatabaseHelper(event.context)
                }
                event.searchQuery.trim().let { query ->
                    if (query.isNotBlank()) {
                        _state.update { it.copy(isLoading = true, sectionSearchResults = emptyList()) }
                        searchJob?.cancel()

                        searchJob = viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                event.searchCategories.forEachIndexed { index, category ->
                                    Log.e(TAG, "SearchCategories: search in progress")

                                    val bookPages =
                                        database?.searchCategory(category, query) ?: emptyList()
                                    val searchLocators = bookPages.mapNotNull { page ->
                                        query.let {
                                            if (page.content.indexOf(it) >= 0)
                                                it
                                            else
                                                null

                                        }?.let { token ->
                                            val queryIndex = page.content.indexOf(token)
                                            Log.e(
                                                TAG,
                                                "onEven: query: [$query], token = [$token], queryIndex: [$queryIndex] , pageContent: [${page.content}]"
                                            )
                                            val textBefore = page.content.run {
                                                substring(maxOf(queryIndex - 50, 0), queryIndex)
                                            }
                                            val textAfter = page.content.run {
                                                substring(
                                                    queryIndex + token.length,
                                                    minOf(queryIndex + token.length + 50, lastIndex)
                                                )
                                            }
                                            page.bookTitle to
                                                    Locator(
                                                        href = page.href,
                                                        created = 0,
                                                        title = page.category,
                                                        locations = Locations(cfi = "...$textBefore $token $textAfter..."),
                                                        text = LocatorText(
                                                            before = textBefore,
                                                            hightlight = token,
                                                            after = textAfter
                                                        )
                                                    ).toSearchLocator()
                                        }

                                    }
                                    _state.update {
                                        it.copy(
                                            sectionSearchResults = it.sectionSearchResults + searchLocators,
                                            searchProgress = ((index + 1f) / (event.searchCategories.size
                                                ?: 1))
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
    }

    override fun onCleared() {
        super.onCleared()
        database?.close()

    }
}
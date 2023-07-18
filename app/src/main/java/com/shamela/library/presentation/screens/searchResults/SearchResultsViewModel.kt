package com.shamela.library.presentation.screens.searchResults


import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Quote
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.domain.usecases.quotes.QuotesUseCases
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    @AssetsRepoImpl private val remoteBooksUseCases: BooksUseCases,
    @FilesRepoImpl private val localBooksUseCases: BooksUseCases,
    private val quotesUseCases: QuotesUseCases,
    private val handle: SavedStateHandle,
    private val application: Application,
) : ViewModel(), BooksDownloadManager.Subscriber {
    private val _searchResultsState = MutableStateFlow<SearchResultsState>(SearchResultsState())
    val searchResultsState = _searchResultsState.asStateFlow()
    private var searchJob: Job? = null
    private val booksDownloadManager = BooksDownloadManager(application.applicationContext)


    fun onEvent(event: SearchResultsEvent) {
        when (event) {
            is SearchResultsEvent.Search -> {
                searchJob = viewModelScope.launch {
                    event.query.trim().let { query ->
                        searchJob?.cancel()

                        _searchResultsState.update {
                            it.copy(
                                isLoading = true,
                                booksResultsList = emptyList(),
                                sectionsResultsList = emptyList(),
                            )
                        }
                        val categoryName =
                            handle.get<String>("categoryName").toString().ifBlank { "all" }
                        val type = handle.get<String>("type").toString()
                        _searchResultsState.update { it.copy(type = type) }
                        val repo = when (type) {
                            "local" -> localBooksUseCases
                            "remote" -> remoteBooksUseCases
                            "sections" -> remoteBooksUseCases
                            else -> localBooksUseCases
                        }
                        launch {
                            if (type == "sections") {
                                _searchResultsState.update {
                                    val newList = repo.getAllCategories().filter { category ->
                                        category.name.contains(query)
                                    }.toList()
                                    it.copy(
                                        sectionsResultsList = newList,
                                        lastQuery = if (newList.isNotEmpty()) query else "",
                                        isLoading = false
                                    )
                                }
                            } else {
                                repo.searchForABook(categoryName, query).collect { book ->
                                    _searchResultsState.update {
                                        val newList = it.booksResultsList + book
                                        it.copy(
                                            booksResultsList = newList,
                                            lastQuery = if (newList.isNotEmpty()) query else "",
                                            isLoading = false
                                        )
                                    }
                                }
                            }
                        }
                        launch {
                            delay(500)
                            if (type == "sections") {
                                _searchResultsState.update {
                                    it.copy(isListEmpty = it.sectionsResultsList.isEmpty())
                                }
                            } else {
                                _searchResultsState.update {
                                    it.copy(isListEmpty = it.booksResultsList.isEmpty())
                                }
                            }
                        }


                    }
                }
            }

            is SearchResultsEvent.OnClickDownloadBook -> {
                viewModelScope.launch {
                    remoteBooksUseCases.getDownloadUri(event.book.categoryName, event.book.title)
                        ?.let { uri ->
                            val downloadId = booksDownloadManager.downloadBook(
                                downloadUri = uri,
                                book = event.book,
                                bookCategory = event.book.categoryName
                            )
                            if (downloadId == BooksDownloadManager.FILE_ALREADY_EXISTS) {
                                Toast.makeText(
                                    application,
                                    "تم تحميل الكتاب من قبل!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                _searchResultsState.update {
                                    it.copy(isLoading = true)
                                }
                            }
                        }
                }
            }


            is SearchResultsEvent.OnSearchQueryChanged -> {
                _searchResultsState.update {
                    it.copy(query = event.newSearchQuery)
                }
            }

            SearchResultsEvent.ClearSearchQuery -> {
                _searchResultsState.update { it.copy(query = "", isLoading = false) }
                searchJob?.cancel()
            }

            is SearchResultsEvent.AddQuoteToFavorite -> {
                viewModelScope.launch {
                    Log.e("SearchResultsViewModel", "AddQuoteToFavorite ${event.quote}")
                    quotesUseCases.saveQuote(event.quote)
                    Toast.makeText(application, "تمت الإضافة بنجاح", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    init {
        BooksDownloadManager.subscribe(this)
    }

    override fun onCleared() {
        super.onCleared()
        BooksDownloadManager.unsubscribe(this)
    }

    override fun onBookDownloaded(book: Book) {
        _searchResultsState.update {
            it.copy(isLoading = false)
        }
    }
}
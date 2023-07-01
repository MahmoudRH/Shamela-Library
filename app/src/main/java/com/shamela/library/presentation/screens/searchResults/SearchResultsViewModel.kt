package com.shamela.library.presentation.screens.searchResults


import android.app.Application
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioreader.ui.activity.searchActivity.SearchEvent
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.screens.sectionBooks.SectionBooksEvent
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    @AssetsRepoImpl private val remoteBooksUseCases: BooksUseCases,
    @FilesRepoImpl private val localBooksUseCases: BooksUseCases,
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
                                resultsList = emptyList()
                            )
                        }
                        val categoryName =
                            handle.get<String>("categoryName").toString().ifBlank { "all" }
                        val type = handle.get<String>("type").toString()
                        _searchResultsState.update { it.copy(type = type) }
                        val repo = when (type) {
                            "local" -> localBooksUseCases
                            "remote" -> remoteBooksUseCases
                            else -> localBooksUseCases
                        }
                        launch {
                            repo.searchForABook(categoryName, query).collect { book ->
                                _searchResultsState.update {
                                    val newList = it.resultsList + book
                                    it.copy(
                                        resultsList = newList,
                                        lastQuery = if (newList.isNotEmpty()) query else "",
                                        isLoading = false
                                    )
                                }
                            }
                        }
                        launch {
                            delay(500)
                            _searchResultsState.update {
                                it.copy(isListEmpty = it.resultsList.isEmpty())
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
                            if (downloadId == BooksDownloadManager.FILE_ALREADY_EXISTS){
                                Toast.makeText(application, "تم تحميل الكتاب من قبل!", Toast.LENGTH_SHORT).show()
                            }else{
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
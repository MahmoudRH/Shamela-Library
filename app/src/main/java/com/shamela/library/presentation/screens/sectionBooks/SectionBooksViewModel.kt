package com.shamela.library.presentation.screens.sectionBooks


import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SectionBooksViewModel @Inject constructor(
    @AssetsRepoImpl private val remoteBooksUseCases: BooksUseCases,
    @FilesRepoImpl private val localBooksUseCases: BooksUseCases,
    private val handle: SavedStateHandle,
    private val application: Application,
) : ViewModel(), BooksDownloadManager.Subscriber {
    private val _sectionBooksState = MutableStateFlow<SectionBooksState>(SectionBooksState())
    val sectionBooksState = _sectionBooksState.asStateFlow()
    private val booksDownloadManager = BooksDownloadManager(application.applicationContext)

    fun onEvent(event: SectionBooksEvent) {
        when (event) {
            is SectionBooksEvent.OnClickDownloadBook -> {
                viewModelScope.launch {
                    remoteBooksUseCases.getDownloadUri(event.book.categoryName, event.book.title)
                        ?.let { uri ->
                            _sectionBooksState.update {
                                it.copy(isLoading = true)
                            }
                            booksDownloadManager.downloadBook(
                                downloadUri = uri,
                                book = event.book,
                                bookCategory = event.book.categoryName
                            )
                        }
                }
            }

            is SectionBooksEvent.LoadBooks -> {
                viewModelScope.launch {
                    val categoryName = handle.get<String>("categoryName").toString()
                    val type = handle.get<String>("type").toString()
                    _sectionBooksState.update { it.copy(type = type) }
                    when (type) {
                        "local" -> {
                            loadLocalBooksOfSection(categoryName)
                        }

                        "remote" -> {
                            launch {
                                loadRemoteBooksOfSection(categoryName)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadRemoteBooksOfSection(categoryName: String) {
        remoteBooksUseCases.getBooksByCategory(categoryName = categoryName).collect { book ->
            _sectionBooksState.update {
                it.copy(
                    books = it.books + mapOf(book.id to book),
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadLocalBooksOfSection(categoryName: String) {
        localBooksUseCases.getBooksByCategory(categoryName = categoryName).collect { book ->
            _sectionBooksState.update {
                it.copy(
                    books = it.books + mapOf(book.id to book),
                    isLoading = false
                )
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
        _sectionBooksState.update {
            it.copy(isLoading = false)
        }
    }

}
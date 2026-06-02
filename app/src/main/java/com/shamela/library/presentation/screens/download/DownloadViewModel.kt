package com.shamela.library.presentation.screens.download


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.DownloadStatus
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.domain.util.BooksGroupingUtil
import com.shamela.library.presentation.screens.library.BooksViewType
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    @AssetsRepoImpl private val booksUseCases: BooksUseCases,
    private val application: Application,
) : ViewModel(), BooksDownloadManager.Subscriber {
    private val _downloadState = MutableStateFlow(DownloadState())
    val downloadState = _downloadState.asStateFlow()
    private val booksDownloadManager = BooksDownloadManager(application.applicationContext)

    fun onEvent(event: DownloadEvent) {
        when (event) {
            is DownloadEvent.OnChangeViewType -> {
                _downloadState.update { it.copy(booksViewType = event.newBooksViewType) }
                when (event.newBooksViewType) {
                    BooksViewType.Sections -> onEvent(DownloadEvent.LoadUserSections)
                    BooksViewType.Books -> onEvent(DownloadEvent.LoadUserBooks)
                }
            }

            DownloadEvent.LoadUserSections -> {
                if (_downloadState.value.sections.isEmpty())
                    viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            booksUseCases.getAllCategories().collect { category ->
                                _downloadState.update { it.copy(sections = it.sections + category) }
                            }
                        }
                    }
            }

            DownloadEvent.LoadUserBooks -> {
                if (_downloadState.value.books.isEmpty() && !_downloadState.value.isLoadingBooks) {
                    _downloadState.update { it.copy(isLoadingBooks = true) }
                    viewModelScope.launch(Dispatchers.IO) {
                        val allBooks = mutableListOf<Book>()
                        val groupedMutable = mutableMapOf<Char, MutableList<Book>>()
                        var currentCategory = ""

                        booksUseCases.getAllBooks().collect { book ->
                            allBooks.add(book)
                            val key = book.title.firstOrNull() ?: '-'
                            groupedMutable.getOrPut(key) { mutableListOf() }.add(book)

                            if (book.categoryName != currentCategory) {
                                currentCategory = book.categoryName
                                val snapshot = groupedMutable.mapValues { it.value.toList() }
                                _downloadState.update { it.copy(books = allBooks.toList(), groupedBooks = snapshot) }
                            }
                        }

                        val finalGrouped = BooksGroupingUtil.groupByFirstChar(allBooks)
                        _downloadState.update {
                            it.copy(books = allBooks.toList(), groupedBooks = finalGrouped, isLoadingBooks = false)
                        }
                    }
                }
            }

            is DownloadEvent.OnClickDownloadBook -> {
                viewModelScope.launch {
                    booksUseCases.getDownloadUri(event.book.categoryName, event.book.title)?.let { uri ->
                        booksDownloadManager.downloadBook(
                            downloadUri = uri,
                            book = event.book,
                            bookCategory = event.book.categoryName
                        )
                    }
                }
            }

            is DownloadEvent.OnClickCancelDownload -> {
                BooksDownloadManager.cancelDownload(event.bookId)
            }
        }
    }

    init {
        BooksDownloadManager.subscribe(this)
        viewModelScope.launch {
            combine(
                BooksDownloadManager.downloadStatusFlow,
                booksUseCases.getDownloadedBooks()
            ) { statusMap, downloadedList ->
                Pair(statusMap, downloadedList.map { it.id }.toSet())
            }.collect { (statusMap, downloadedIds) ->
                statusMap.forEach { (bookId, status) ->
                    if (status is DownloadStatus.Downloading && downloadedIds.contains(bookId)) {
                        BooksDownloadManager.clearStatus(bookId)
                    }
                }
                _downloadState.update {
                    it.copy(downloadStatuses = statusMap, downloadedBookIds = downloadedIds)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        BooksDownloadManager.unsubscribe(this)
    }

    override fun onBookDownloaded(book: Book, isLastBook: Boolean) {
        // No-op: UI state is driven by the combine collector above
    }
}

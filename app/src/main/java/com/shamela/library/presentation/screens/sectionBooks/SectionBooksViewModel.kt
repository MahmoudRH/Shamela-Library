package com.shamela.library.presentation.screens.sectionBooks


import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.DownloadStatus
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.domain.usecases.quotes.QuotesUseCases
import com.shamela.library.domain.util.BookSortOption
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SectionBooksViewModel @Inject constructor(
    @AssetsRepoImpl private val remoteBooksUseCases: BooksUseCases,
    @FilesRepoImpl private val localBooksUseCases: BooksUseCases,
    private val handle: SavedStateHandle,
    private val quotesUseCases: QuotesUseCases,
    private val application: Application
) : ViewModel(), BooksDownloadManager.Subscriber {
    private val _sectionBooksState = MutableStateFlow(SectionBooksState())
    val sectionBooksState = _sectionBooksState.asStateFlow()
    private val booksDownloadManager = BooksDownloadManager(application.applicationContext)

    fun onEvent(event: SectionBooksEvent) {
        when (event) {
            SectionBooksEvent.OnClickDownloadSection -> {
                viewModelScope.launch {
                    val categoryName = handle.get<String>("categoryName").toString()
                    if (sectionBooksState.value.books.isEmpty()) {
                        loadRemoteBooksOfSection(categoryName)
                    }
                    val bookUriMap = sectionBooksState.value.books.values.associateWith { book ->
                        async { remoteBooksUseCases.getDownloadUri(categoryName, book.title) }
                    }
                    val bookUriList = withContext(coroutineContext) {
                        bookUriMap.mapValues { it.value.await() }
                    }
                    Log.d("SectionBooksViewModel", "bookUriList: ${bookUriList.values}")
                    booksDownloadManager.downloadSection(bookUriList)
                    _sectionBooksState.update { it.copy(isDownloadButtonEnabled = false) }
                }
            }

            is SectionBooksEvent.OnClickDownloadBook -> {
                viewModelScope.launch {
                    remoteBooksUseCases.getDownloadUri(event.book.categoryName, event.book.title)
                        ?.let { uri ->
                            booksDownloadManager.downloadBook(
                                downloadUri = uri,
                                book = event.book,
                                bookCategory = event.book.categoryName
                            )
                        }
                }
            }

            is SectionBooksEvent.OnClickCancelDownload -> {
                BooksDownloadManager.cancelDownload(event.bookId)
            }

            is SectionBooksEvent.LoadBooks -> {
                viewModelScope.launch {
                    val categoryName = handle.get<String>("categoryName").toString()
                    val type = handle.get<String>("type").toString()
                    _sectionBooksState.update { it.copy(type = type) }
                    when (type) {
                        "local" -> loadLocalBooksOfSection(categoryName)
                        "remote" -> launch { loadRemoteBooksOfSection(categoryName) }
                    }
                }
            }

            is SectionBooksEvent.AddQuoteToFavorite -> {
                viewModelScope.launch {
                    Log.d("SectionBooksViewModel", "AddQuoteToFavorite ${event.quote}")
                    quotesUseCases.saveQuote(event.quote)
                    Toast.makeText(application, "تمت الإضافة بنجاح", Toast.LENGTH_SHORT).show()
                }
            }

            is SectionBooksEvent.OnChangeSortOption -> {
                _sectionBooksState.update {
                    it.copy(
                        sortOption = event.option,
                        sortAscending = event.option.defaultAscending
                    )
                }
                if (event.option == BookSortOption.DOWNLOAD_TIME) ensureDownloadTimes()
            }

            SectionBooksEvent.OnToggleSortDirection -> {
                _sectionBooksState.update { it.copy(sortAscending = !it.sortAscending) }
            }
        }
    }

    private var downloadTimesJob: Job? = null

    /**
     * Lazily fills [SectionBooksState.downloadTimes] with each local book's file last-modified time.
     * Only meaningful for local sections (remote books have no file); runs on IO since StrictMode
     * forbids file access on the main thread.
     */
    private fun ensureDownloadTimes() {
        if (sectionBooksState.value.type != "local") return
        if (downloadTimesJob?.isActive == true) return
        downloadTimesJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val current = _sectionBooksState.value
                val missing = current.books.keys - current.downloadTimes.keys
                if (missing.isEmpty()) break
                val newTimes = missing.associateWith { id ->
                    val book = current.books[id] ?: return@associateWith 0L
                    File(BooksDownloadManager.getBookPath(book))
                        .takeIf { it.isFile }?.lastModified() ?: 0L
                }
                _sectionBooksState.update { it.copy(downloadTimes = it.downloadTimes + newTimes) }
            }
        }
    }

    private suspend fun loadRemoteBooksOfSection(categoryName: String) {
        remoteBooksUseCases.getBooksByCategory(categoryName = categoryName).collect { book ->
            _sectionBooksState.update {
                it.copy(books = it.books + mapOf(book.id to book), isLoading = false)
            }
        }
    }

    private suspend fun loadLocalBooksOfSection(categoryName: String) {
        localBooksUseCases.getBooksByCategory(categoryName = categoryName).collect { book ->
            _sectionBooksState.update {
                it.copy(books = it.books + mapOf(book.id to book), isLoading = false)
            }
            if (_sectionBooksState.value.sortOption == BookSortOption.DOWNLOAD_TIME) {
                ensureDownloadTimes()
            }
        }
    }

    init {
        BooksDownloadManager.subscribe(this)
        viewModelScope.launch {
            combine(
                BooksDownloadManager.downloadStatusFlow,
                localBooksUseCases.getDownloadedBooks()
            ) { statusMap, downloadedList ->
                Pair(statusMap, downloadedList.map { it.id }.toSet())
            }.collect { (statusMap, downloadedIds) ->
                statusMap.forEach { (bookId, status) ->
                    if (status is DownloadStatus.Downloading && downloadedIds.contains(bookId)) {
                        BooksDownloadManager.clearStatus(bookId)
                    }
                }
                _sectionBooksState.update {
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
        if (!sectionBooksState.value.isDownloadButtonEnabled && isLastBook) {
            _sectionBooksState.update { it.copy(isDownloadButtonEnabled = true) }
        }
    }
}

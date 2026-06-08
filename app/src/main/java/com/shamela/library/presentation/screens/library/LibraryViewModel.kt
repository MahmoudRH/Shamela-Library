package com.shamela.library.presentation.screens.library


import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.domain.usecases.quotes.QuotesUseCases
import com.shamela.library.domain.util.BookSortOption
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @FilesRepoImpl private val booksUseCases: BooksUseCases,
    private val quotesUseCases: QuotesUseCases,
    private val application: Application,
) : ViewModel(), BooksDownloadManager.Subscriber {
    private val _libraryState = MutableStateFlow<LibraryState>(LibraryState())
    val libraryState = _libraryState.asStateFlow()

    /** Map of Book Id -> isFavorite*/

    init {
        onEvent(LibraryEvent.LoadUserBooksAndSections)
        BooksDownloadManager.subscribe(this)
    }

    fun onEvent(event: LibraryEvent) {
        when (event) {
            is LibraryEvent.OnChangeViewType -> {
                _libraryState.update { it.copy(booksViewType = event.newBooksViewType) }
            }

            LibraryEvent.LoadUserBooksAndSections -> {
                viewModelScope.launch {
                    Log.e("Mah ", "LibraryViewModel: loading Books")
                    booksUseCases.getAllBooks().onEach {
                        //reading the files under the downloads folder, saving them to database if they're not already (for externally added files)
                        //another case is, when the app data is cleared or the app is deleted then downloaded again.
                        if (it.pageCount > 0)  /* books are initially not parsed*/
                            booksUseCases.saveDownloadedBook(it)
                    }.launchIn(this)
                    booksUseCases.getDownloadedBooks().onEach {
                        val databaseBooks = it.associateBy { book -> book.id }
                        _libraryState.update { state ->
                            state.copy(
                                books = state.books + databaseBooks,
                                isLoading = false
                            )
                        }
                        // Keep download times in sync with newly arrived books while that sort is active.
                        if (_libraryState.value.sortOption == BookSortOption.DOWNLOAD_TIME) {
                            ensureDownloadTimes()
                        }
                    }.launchIn(this)
                    launch {
                        booksUseCases.getAllCategories().collect { category ->
                            _libraryState.update {
                                it.copy(
                                    sections = it.sections + mapOf(category.id to category),
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }

            is LibraryEvent.ToggleFavorite -> {
                val book = event.book
                val newState = !book.isFavorite
                _libraryState.update {
                    it.copy(
                        books = it.books + mapOf(book.id to book.copy(isFavorite = newState)),
                    )
                }
                viewModelScope.launch {
                    booksUseCases.updateBook(book.id, if (newState) 1 else 0)
                }
            }

            is LibraryEvent.AddQuoteToFavorite -> {
                viewModelScope.launch {
                    Log.e("LibraryViewModel", "AddQuoteToFavorite ${event.quote}")
                    quotesUseCases.saveQuote(event.quote)
                    Toast.makeText(application, "تمت الإضافة بنجاح", Toast.LENGTH_SHORT).show()
                }
            }

            is LibraryEvent.DeleteBook -> {
                viewModelScope.launch {
                    val book = event.book
                    booksUseCases.deleteBook(book).let { isDeleteSuccess ->
                        if (isDeleteSuccess) {
                            _libraryState.update { it.copy(books = it.books - book.id) }
                            launch {
                                // Reload library sections, to update books count and exclude empty sections
                                _libraryState.update { it.copy(sections = emptyMap()) }
                                booksUseCases.getAllCategories().collect { category ->
                                    _libraryState.update {
                                        it.copy(sections = it.sections + mapOf(category.id to category), isLoading = false)
                                    }
                                }
                            }
                        }
                    }

                }
            }

            is LibraryEvent.SelectBook -> {
                val isBookSelected = libraryState.value.selectedBooks.contains(event.book)
                _libraryState.update {
                    it.copy(
                        selectedBooks =
                        if (isBookSelected)
                            it.selectedBooks - event.book
                        else
                            it.selectedBooks + event.book
                    )
                }
            }

            LibraryEvent.CancelSelection -> {
                _libraryState.update { it.copy(selectedBooks = emptyList()) }
            }

            LibraryEvent.DeleteSelectedBooks -> {
                libraryState.value.selectedBooks.forEach {
                    onEvent(LibraryEvent.DeleteBook(it))
                }
                _libraryState.update { it.copy(selectedBooks = emptyList()) }
            }

            is LibraryEvent.OnChangeSortOption -> {
                _libraryState.update {
                    it.copy(
                        sortOption = event.option,
                        sortAscending = event.option.defaultAscending
                    )
                }
                if (event.option == BookSortOption.DOWNLOAD_TIME) ensureDownloadTimes()
            }

            LibraryEvent.OnToggleSortDirection -> {
                _libraryState.update { it.copy(sortAscending = !it.sortAscending) }
            }
        }
    }

    private var downloadTimesJob: Job? = null

    /**
     * Lazily fills [LibraryState.downloadTimes] with the file last-modified time of every book that
     * doesn't have one yet. mtime never changes, so this only ever stats files once per book. Runs
     * on IO (StrictMode forbids file access on the main thread) and loops to absorb books that arrive
     * while it's working.
     */
    private fun ensureDownloadTimes() {
        if (downloadTimesJob?.isActive == true) return
        downloadTimesJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val current = _libraryState.value
                val missing = current.books.keys - current.downloadTimes.keys
                if (missing.isEmpty()) break
                val newTimes = missing.associateWith { id ->
                    val book = current.books[id] ?: return@associateWith 0L
                    File(BooksDownloadManager.getBookPath(book))
                        .takeIf { it.isFile }?.lastModified() ?: 0L
                }
                _libraryState.update { it.copy(downloadTimes = it.downloadTimes + newTimes) }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        BooksDownloadManager.unsubscribe(this)
    }

    override fun onBookDownloaded(book: Book, isLastBook: Boolean) {
        _libraryState.update {
            it.copy(
                books = it.books + (book.id to book),
                isLoading = false
            )
        }
        if (_libraryState.value.sortOption == BookSortOption.DOWNLOAD_TIME) ensureDownloadTimes()
        viewModelScope.launch {

            launch {
                //update categories section with the new book
                booksUseCases.getAllCategories().collect { category ->
                    _libraryState.update {
                        it.copy(
                            sections = it.sections + mapOf(category.id to category),
                            isLoading = false
                        )
                    }
                }
            }
            /*            launch {
                            //save the downloaded book to the database
                            booksUseCases.saveDownloadedBook(book)
                        }*/
        }

    }
}
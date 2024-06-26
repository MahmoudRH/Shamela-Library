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
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
package com.shamela.library.presentation.screens.library


import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitAll
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
                _libraryState.update { it.copy(viewType = event.newViewType) }
            }

            LibraryEvent.LoadUserBooksAndSections -> {
                viewModelScope.launch {
                    Log.e("Mah ", "LibraryViewModel: loading Books")
                    booksUseCases.getDownloadedBooks().onEach {
                        val books = it.associateBy { book -> book.id }
                        _libraryState.update { state ->
                            state.copy(
                                books = state.books + books,
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
        }
    }


    override fun onCleared() {
        super.onCleared()
        BooksDownloadManager.unsubscribe(this)
    }

    override fun onBookDownloaded(book: Book) {
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
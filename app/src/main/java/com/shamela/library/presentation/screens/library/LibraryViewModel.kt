package com.shamela.library.presentation.screens.library


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.usecases.books.BooksUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @FilesRepoImpl private val booksUseCases: BooksUseCases,
) : ViewModel() {
    private val _libraryState = MutableStateFlow<LibraryState>(LibraryState())
    val libraryState = _libraryState.asStateFlow()

    init {
        onEvent(LibraryEvent.LoadUserBooksAndSections)
    }

    fun onEvent(event: LibraryEvent) {
        when (event) {
            is LibraryEvent.OnChangeViewType -> {
                _libraryState.update { it.copy(viewType = event.newViewType) }
            }

            LibraryEvent.LoadUserBooksAndSections -> {
                viewModelScope.launch {
                    Log.e("Mah ", "LibraryViewModel: loading Books")
                    launch {
                        booksUseCases.getAllBooks().collect { book ->
                            _libraryState.update {
                                it.copy(
                                    books = it.books + mapOf(book.id to book),
                                    isLoading = false
                                )
                            }
                        }
                    }
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
        }
    }
}

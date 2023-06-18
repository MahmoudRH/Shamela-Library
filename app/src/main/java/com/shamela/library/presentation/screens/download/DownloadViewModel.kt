package com.shamela.library.presentation.screens.download


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    @AssetsRepoImpl private val booksUseCases: BooksUseCases,
    private val application: Application,
) : ViewModel() {
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState())
    val downloadState = _downloadState.asStateFlow()


    init {
        onEvent(DownloadEvent.LoadUserBooksAndSections)
    }

    fun onEvent(event: DownloadEvent) {
        when (event) {
            is DownloadEvent.OnChangeViewType -> {
                _downloadState.update { it.copy(viewType = event.newViewType) }
            }

            DownloadEvent.LoadUserBooksAndSections -> {
                viewModelScope.launch {
                    launch {
                        booksUseCases.getAllBooks().collect { book ->
                            _downloadState.update {
                                it.copy(
                                    books = if (it.books.contains(book)) it.books else it.books + book,
                                    isLoading = false
                                )
                            }
                        }
                    }
                    launch {
                        booksUseCases.getAllCategories().collect { category ->
                            _downloadState.update {
                                it.copy(sections = it.sections + category, isLoading = false)
                            }
                        }
                    }
                }
            }

            is DownloadEvent.OnClickDownloadBook -> {
                viewModelScope.launch {
                    // downloading state
                    booksUseCases.getDownloadUri(event.book.categoryName, event.book.title)?.let {
                        BooksDownloadManager(application.applicationContext).downloadBook(
                            downloadUri = it,
                            book = event.book,
                            bookCategory = event.book.categoryName
                        )
                    }

                }
            }
        }
    }
}
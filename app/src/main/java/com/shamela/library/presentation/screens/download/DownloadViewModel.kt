package com.shamela.library.presentation.screens.download


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.domain.usecases.books.BooksUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(private val booksUseCases: BooksUseCases) :
    ViewModel() {
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
                    _downloadState.update { it.copy(isLoading = true) }
                    val availableBooks = booksUseCases.getAllBooks()
                    val userSections = booksUseCases.getAllCategories()
                    _downloadState.update {
                        it.copy(
                            books = availableBooks,
                            sections = userSections.sortedBy {section-> section.name },
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}
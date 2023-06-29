package com.shamela.library.presentation.screens.download


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.screens.library.ViewType
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    @AssetsRepoImpl private val booksUseCases: BooksUseCases,
    private val application: Application,
) : ViewModel() {
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState())
    val downloadState = _downloadState.asStateFlow()


    fun onEvent(event: DownloadEvent) {
        when (event) {
            is DownloadEvent.OnChangeViewType -> {
                _downloadState.update { it.copy(viewType = event.newViewType) }
                when (event.newViewType) {
                    ViewType.Sections -> onEvent(DownloadEvent.LoadUserSections)
                    ViewType.Books -> onEvent(DownloadEvent.LoadUserBooks)
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
                if (_downloadState.value.books.isEmpty())
                    viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            booksUseCases.getAllBooks().collect { book ->
                                _downloadState.update { it.copy(books = it.books + book) }
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
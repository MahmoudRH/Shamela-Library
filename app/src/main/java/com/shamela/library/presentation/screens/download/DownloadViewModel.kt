package com.shamela.library.presentation.screens.download


import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.screens.library.BooksViewType
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
) : ViewModel(), BooksDownloadManager.Subscriber {
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState())
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
                    booksUseCases.getDownloadUri(event.book.categoryName, event.book.title)?.let {uri->
                        val downloadId = booksDownloadManager.downloadBook(
                            downloadUri = uri,
                            book = event.book,
                            bookCategory = event.book.categoryName
                        )
                        if (downloadId == BooksDownloadManager.FILE_ALREADY_EXISTS){
                            Toast.makeText(application, "تم تحميل الكتاب من قبل!", Toast.LENGTH_SHORT).show()
                        }else{
                            _downloadState.update {
                                it.copy(isLoading = true)
                            }
                        }
                    }
                }
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

    override fun onBookDownloaded(book: Book, isLastBook:Boolean) {
        _downloadState.update {
            it.copy(isLoading = false)
        }
    }
}
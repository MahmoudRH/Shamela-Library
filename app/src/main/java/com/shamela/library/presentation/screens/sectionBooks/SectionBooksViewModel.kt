package com.shamela.library.presentation.screens.sectionBooks


import android.app.Application
import android.app.DownloadManager
import android.content.IntentFilter
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.reciever.DownloadCompleteReceiver
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SectionBooksViewModel @Inject constructor(
    @AssetsRepoImpl private val booksUseCases: BooksUseCases,
    private val handle: SavedStateHandle,
    private val application: Application,
) : ViewModel() {
    private val _sectionBooksState = MutableStateFlow<SectionBooksState>(SectionBooksState())
    val sectionBooksState = _sectionBooksState.asStateFlow()
    private val booksDownloadManager = BooksDownloadManager(application.applicationContext)
    private val downloadCompleteReceiver = DownloadCompleteReceiver()

    init {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        application.applicationContext.registerReceiver(downloadCompleteReceiver, filter)
    }

    fun onEvent(event: SectionBooksEvent) {
        when (event) {
            is SectionBooksEvent.OnClickDownloadBook -> {
                viewModelScope.launch {
                    booksUseCases.getDownloadUri(event.book.categoryName, event.book.title)
                        ?.let { uri ->
                            booksDownloadManager.downloadBook(
                                downloadUri = uri,
                                book = event.book,
                                bookCategory = event.book.categoryName
                            )
                        }
                }
            }

            is SectionBooksEvent.LoadBooks -> {
                viewModelScope.launch {
                    val categoryName = handle.get<String>("categoryName").toString()
                    val type = handle.get<String>("type").toString()
                    when (type) {
                        "local" -> {
                            loadLocalBooksOfSection(categoryName)
                        }

                        "remote" -> {
                            launch{
                                loadRemoteBooksOfSection(categoryName)
                            }
                        }
                    }
                }
            }


        }
    }

    private suspend fun loadRemoteBooksOfSection(categoryName: String) {
        booksUseCases.getBooksByCategory(categoryName = categoryName).collect { book ->
            _sectionBooksState.update { it.copy(books = it.books + book, isLoading = false) }
        }
    }

    private suspend fun loadLocalBooksOfSection(sectionId: String) {
        //TODO: When the time comes
    }

    override fun onCleared() {
        super.onCleared()
        application.applicationContext.unregisterReceiver(downloadCompleteReceiver)
    }

}
package com.shamela.library.presentation.common

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.assets.AssetsBooksRepoImpl
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.BookDetails
import com.shamela.library.domain.model.DownloadStatus
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.domain.usecases.quotes.QuotesUseCases
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the book-details screen: loads the "about the book" metadata (from the enriched
 * assets/book-details JSON) and drives the bottom action button — reflecting download/downloaded
 * state and triggering download or open. Reuses the same [BooksDownloadManager] + use-cases as the
 * library/section screens, so state stays consistent app-wide (saving to Room is done by the
 * download-complete receiver, independent of this screen).
 */
@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val assetsBooksRepo: AssetsBooksRepoImpl,
    @AssetsRepoImpl private val remoteBooksUseCases: BooksUseCases,
    @FilesRepoImpl private val localBooksUseCases: BooksUseCases,
    private val quotesUseCases: QuotesUseCases,
    application: Application,
) : ViewModel() {

    data class State(
        val isLoading: Boolean = true,
        val details: BookDetails? = null,
        val isDownloaded: Boolean = false,
        val isDownloading: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val booksDownloadManager = BooksDownloadManager(application.applicationContext)
    private var book: Book? = null

    fun load(book: Book) {
        if (this.book?.id == book.id) return   // already bound (guards recomposition)
        this.book = book

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val details = assetsBooksRepo.getBookDetails(book.categoryName, book.title)
            _state.update { it.copy(isLoading = false, details = details) }
        }

        // Reactively track download/downloaded state for this single book.
        viewModelScope.launch {
            combine(
                BooksDownloadManager.downloadStatusFlow,
                localBooksUseCases.getDownloadedBooks(),
            ) { statusMap, downloadedList ->
                val downloaded = downloadedList.any { it.id == book.id }
                val downloading = statusMap[book.id] is DownloadStatus.Downloading
                downloaded to downloading
            }.collect { (downloaded, downloading) ->
                if (downloaded && downloading) BooksDownloadManager.clearStatus(book.id)
                _state.update {
                    it.copy(isDownloaded = downloaded, isDownloading = downloading && !downloaded)
                }
            }
        }
    }

    fun downloadBook() {
        val book = book ?: return
        // Optimistic: disable the button immediately so a double-tap can't fire two downloads
        // during the (suspend) download-uri lookup, before the Downloading status arrives.
        _state.update { it.copy(isDownloading = true) }
        viewModelScope.launch {
            val uri = remoteBooksUseCases.getDownloadUri(book.categoryName, book.title)
            if (uri != null) {
                booksDownloadManager.downloadBook(uri, book, book.categoryName)
            } else {
                _state.update { it.copy(isDownloading = false) }
            }
        }
    }

    fun openBook() {
        val book = book ?: return
        FilesBooksRepoImpl.openEpub(
            book = book,
            onAddQuoteToFavorite = { quote -> viewModelScope.launch { quotesUseCases.saveQuote(quote) } },
        )
    }
}

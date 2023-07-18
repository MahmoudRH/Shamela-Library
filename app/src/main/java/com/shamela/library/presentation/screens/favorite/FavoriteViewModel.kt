package com.shamela.library.presentation.screens.favorite


import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.model.Quote
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.domain.usecases.quotes.QuotesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    @FilesRepoImpl private val booksUseCases: BooksUseCases,
    private val quotesUseCases: QuotesUseCases,
    private val application: Application,
) : ViewModel() {
    private val _favoriteState = MutableStateFlow<FavoriteState>(FavoriteState())
    val favoriteState = _favoriteState.asStateFlow()

    fun onEvent(event: FavoriteEvent) {
        when (event) {
            is FavoriteEvent.LoadFavoriteBooks -> {
                viewModelScope.launch {
                    Log.e("FavoriteViewModel", "onEvent: LoadFavoriteBooks ", )
                    booksUseCases.getFavoriteBooks().onEach { books ->
                        _favoriteState.update { it.copy(favoriteBooks = books, isListEmpty = books.isEmpty()) }
                    }.launchIn(this@launch)
                }
            }
            FavoriteEvent.LoadFavoriteQuotes ->{
                viewModelScope.launch {
                    Log.e("FavoriteViewModel", "onEvent: LoadFavoriteQuotes ", )
                    quotesUseCases.getAllQuotes().onEach { quotes ->
                        _favoriteState.update { it.copy(favoriteQuotes = quotes, isListEmpty = quotes.isEmpty()) }
                    }.launchIn(this@launch)
                }
            }

            is FavoriteEvent.ToggleFavorite -> {
                val book = event.book
                val newState = !book.isFavorite
                viewModelScope.launch {
                    booksUseCases.updateBook(book.id, if (newState) 1 else 0)
                }
            }

            is FavoriteEvent.AddQuoteToFavorite -> {
                viewModelScope.launch {
                    Log.e("FavoriteViewModel", "AddQuoteToFavorite ${event.quote}")
                    quotesUseCases.saveQuote(event.quote)
                    Toast.makeText(application, "تمت الإضافة بنجاح", Toast.LENGTH_SHORT).show()
                }
            }

            is FavoriteEvent.OnChangeViewType -> {
                _favoriteState.update { it.copy(viewType = event.newViewType) }
                when(event.newViewType){
                    FavoriteViewType.Quotes -> onEvent(FavoriteEvent.LoadFavoriteQuotes)
                    FavoriteViewType.Books -> onEvent(FavoriteEvent.LoadFavoriteBooks)
                }
            }

            is FavoriteEvent.OpenBookForQuote ->{
                viewModelScope.launch {
                    booksUseCases.getBookById(event.currentQuote.bookId)?.let{
                        FilesBooksRepoImpl.openEpub(it, event.currentQuote.pageHref) { newQuote ->
                            onEvent(FavoriteEvent.AddQuoteToFavorite(newQuote))
                        }
                    }
                }
            }

        }
    }

}
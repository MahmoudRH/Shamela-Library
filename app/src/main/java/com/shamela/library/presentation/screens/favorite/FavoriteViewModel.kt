package com.shamela.library.presentation.screens.favorite


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.usecases.books.BooksUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    @FilesRepoImpl private val booksUseCases: BooksUseCases,
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

            is FavoriteEvent.ToggleFavorite -> {
                val book = event.book
                val newState = !book.isFavorite
                viewModelScope.launch {
                    booksUseCases.updateBook(book.id, if (newState) 1 else 0)
                }
            }
        }
    }

}
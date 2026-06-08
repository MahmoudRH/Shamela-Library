package com.shamela.library.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.assets.AssetsBooksRepoImpl
import com.shamela.library.domain.model.BookDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Loads the "about the book" metadata for whichever book the [BookDetailsBottomSheet] is showing.
 * Backed by the enriched assets/book-details JSON via [AssetsBooksRepoImpl.getBookDetails].
 */
@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val assetsBooksRepo: AssetsBooksRepoImpl,
) : ViewModel() {

    data class State(
        val isLoading: Boolean = true,
        val details: BookDetails? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun load(categoryName: String, bookTitle: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val details = assetsBooksRepo.getBookDetails(categoryName, bookTitle)
            _state.update { State(isLoading = false, details = details) }
        }
    }
}

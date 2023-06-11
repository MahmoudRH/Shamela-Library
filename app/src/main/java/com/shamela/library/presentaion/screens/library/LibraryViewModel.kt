package com.shamela.library.presentaion.screens.library


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.presentaion.utils.FakeRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor() : ViewModel() {
    private val _libraryState = MutableStateFlow<LibraryState>(LibraryState())
    val libraryState = _libraryState.asStateFlow()

    init{
        onEvent(LibraryEvent.LoadUserBooksAndSections)
    }
    fun onEvent(event: LibraryEvent) {
        when (event) {
            is LibraryEvent.OnChangeViewType -> {
                _libraryState.update { it.copy(viewType = event.newViewType) }
            }

            LibraryEvent.LoadUserBooksAndSections -> {
                viewModelScope.launch {
                    _libraryState.update { it.copy(isLoading = true) }
                    val userBooks = FakeRepo.getUserBooks()
                    val userSections = FakeRepo.getUserSections()
                    _libraryState.update {
                        it.copy(
                            books = userBooks + userBooks + userBooks,
                            sections = userSections + userSections + userSections,
                            isLoading = false
                        )
                    }

                }
            }
        }
    }

}
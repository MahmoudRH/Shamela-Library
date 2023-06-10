package com.shamela.library.presentaion.screens.library


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor():ViewModel() {
    private val _libraryState = MutableStateFlow<LibraryState>(LibraryState())
    val libraryState = _libraryState.asStateFlow()


    fun onEvent(event: LibraryEvent) {
      when (event) {
          is LibraryEvent.SampleEvent -> {
             _libraryState.update { it.copy(example = event.newText) }
           }
        }
   }

}
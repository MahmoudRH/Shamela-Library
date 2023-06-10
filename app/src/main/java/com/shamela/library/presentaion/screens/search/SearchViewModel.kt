package com.shamela.library.presentaion.screens.search


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor():ViewModel() {
    private val _searchState = MutableStateFlow<SearchState>(SearchState())
    val searchState = _searchState.asStateFlow()


    fun onEvent(event: SearchEvent) {
      when (event) {
          is SearchEvent.SampleEvent -> {
             _searchState.update { it.copy(example = event.newText) }
           }
        }
   }

}
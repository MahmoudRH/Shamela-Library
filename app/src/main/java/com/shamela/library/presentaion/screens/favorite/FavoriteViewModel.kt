package com.shamela.library.presentaion.screens.favorite


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor():ViewModel() {
    private val _favoriteState = MutableStateFlow<FavoriteState>(FavoriteState())
    val favoriteState = _favoriteState.asStateFlow()


    fun onEvent(event: FavoriteEvent) {
      when (event) {
          is FavoriteEvent.SampleEvent -> {
             _favoriteState.update { it.copy(example = event.newText) }
           }
        }
   }

}
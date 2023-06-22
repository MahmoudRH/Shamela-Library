package com.folioreader.ui.screens.tableOfContent


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TableOfContentViewModel @Inject constructor():ViewModel() {
    private val _tableOfContentState = MutableStateFlow<TableOfContentState>(TableOfContentState())
    val tableOfContentState = _tableOfContentState.asStateFlow()


    fun onEvent(event: TableOfContentEvent) {
      when (event) {
          is TableOfContentEvent.SampleEvent -> {
             _tableOfContentState.update { it.copy(example = event.newText) }
           }
        }
   }

}
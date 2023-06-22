package com.folioreader.ui.screens.highlights


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioreader.model.sqlite.HighLightTable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HighlightsViewModel @Inject constructor() : ViewModel() {
    private val _highlightsState = MutableStateFlow<HighlightsState>(HighlightsState())
    val highlightsState = _highlightsState.asStateFlow()


    fun onEvent(event: HighlightsEvent) {
        when (event) {
            is HighlightsEvent.GetHighlightsOf -> {
                viewModelScope.launch {
                    val list = HighLightTable.getAllHighlights(bookId = event.bookId).toList()
                    _highlightsState.update {
                        it.copy(highlightsList = list, isLoading = false)
                    }
                }

            }

            is HighlightsEvent.DeleteItem -> {
                _highlightsState.update {
                    it.copy(highlightsList = it.highlightsList - event.item)
                }
            }
        }
    }

}
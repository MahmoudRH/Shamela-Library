package com.folioreader.ui.screens.tableOfContent


import androidx.lifecycle.ViewModel
import com.folioreader.model.TOCLinkWrapper
import com.folioreader.ui.fragment.TableOfContentFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.readium.r2.shared.Link
import javax.inject.Inject

@HiltViewModel
class TableOfContentViewModel @Inject constructor() : ViewModel() {
    private val _tableOfContentState = MutableStateFlow<TableOfContentState>(TableOfContentState())
    val tableOfContentState = _tableOfContentState.asStateFlow()


    fun onEvent(event: TableOfContentEvent) {
        when (event) {
            is TableOfContentEvent.LoadTableOfContentsOf -> {
                _tableOfContentState.update {
                    it.copy(
                        items = event.publication.tableOfContents.ifEmpty { event.publication.readingOrder },
                        isLoading = false
                    )
                }
            }
        }
    }

}
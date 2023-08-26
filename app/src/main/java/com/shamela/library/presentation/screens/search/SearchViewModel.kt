package com.shamela.library.presentation.screens.search


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.apptheme.data.db.DatabaseHelper
import com.shamela.library.data.local.assets.AssetsRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.utils.FakeRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @FilesRepoImpl private val booksUseCases: BooksUseCases,
) : ViewModel() {
    private val _searchState = MutableStateFlow<SearchState>(SearchState())
    val searchState = _searchState.asStateFlow()
    private var searchJob: Job? = null
    private var database: DatabaseHelper? = null


    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.SampleEvent -> {
                _searchState.update { it.copy(example = event.newText) }
            }

            is SearchEvent.GetAllCategories -> {
                viewModelScope.launch {
                    _searchState.update { it.copy(isLoading = true) }
                    if (_searchState.value.allCategories.isEmpty()){
                    booksUseCases.getAllCategories().collect { category ->
                        _searchState.update {
                                it.copy(
                                    allCategories = it.allCategories + category,
                                    isLoading = false
                                )
                            }
                        }
                    }else{
                        _searchState.update { it.copy(allCategories = emptyList()) }
                        booksUseCases.getAllCategories().collect { category ->
                            _searchState.update {
                                it.copy(
                                    allCategories = it.allCategories + category,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }

            SearchEvent.ToggleCategoriesList -> {
                _searchState.update { it.copy(isListExpanded = !it.isListExpanded) }
            }

            SearchEvent.CloseCategoriesList -> {
                _searchState.update { it.copy(isListExpanded = false) }
            }

            is SearchEvent.ItemChecked -> {
                _searchState.update {
                    it.copy(
                        selectedCategories =
                        if (it.selectedCategories.contains(event.category))
                            it.selectedCategories - event.category
                        else
                            it.selectedCategories + event.category
                    )
                }
            }

            is SearchEvent.OnChangeSearchQuery -> {
                _searchState.update {
                    it.copy(searchQuery = event.newText)
                }
            }

            is SearchEvent.DoSearch -> {
                if (database === null) {
                    database = DatabaseHelper(event.context)
                }
                event.query.trim().let { query ->
                    if (query.isNotBlank()) {
                        _searchState.update {
                            it.copy(
                                isLoading = true,
                                searchResults = emptyList()
                            )
                        }
                        searchJob?.cancel()

                        searchJob = viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                event.selectedCategories.forEach { category ->
                                    val bookPages =
                                        database?.searchCategory(category, query) ?: emptyList()

                                }
                            }

                        }
                    }
                }
            }
        }

    }
}

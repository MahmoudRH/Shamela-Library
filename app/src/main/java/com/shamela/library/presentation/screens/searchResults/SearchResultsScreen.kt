package com.shamela.library.presentation.screens.searchResults


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shamela.apptheme.presentation.common.EmptyListScreen
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.common.SearchTopBar
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.presentation.common.BookItem
import com.shamela.library.presentation.common.SectionItem

@Composable
fun SearchResultsScreen(
    viewModel: SearchResultsViewModel = hiltViewModel(),
    navigateToSectionBooksScreen: (categoryName: String, type: String) -> Unit,
    navigateBack: () -> Unit,
) {
    val state = viewModel.searchResultsState.collectAsState().value
    val focusRequester = FocusRequester()
    Column(Modifier.fillMaxSize()) {
        SearchTopBar(
            onNavigateBack = navigateBack,
            hint = "بحث..",
            focusRequester = focusRequester,
            value = state.query,
            onValueChanged = { viewModel.onEvent((SearchResultsEvent.OnSearchQueryChanged(it))) },
            onClickClear = { viewModel.onEvent(SearchResultsEvent.ClearSearchQuery) },
            onClickSearch = { query -> viewModel.onEvent(SearchResultsEvent.Search(query)) },
        )
        LoadingScreen(visibility = state.isLoading)
        EmptyListScreen(
            visibility = state.isListEmpty,
            text = "لم يتم العثور على أي نتائج..",
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (state.type == "sections"){
                    items(state.sectionsResultsList, key = {it.id}){
                        SectionItem(modifier = Modifier
                            .clickable {
                                navigateToSectionBooksScreen(it.name, "remote")
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                            item = it,
                            highlightText = state.lastQuery)
                        Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                    }
                }else{
                    items(state.booksResultsList, key = {it.id}) { currentBook->
                        when (state.type){
                            "local"->{
                                BookItem(modifier = Modifier
                                    .clickable { FilesBooksRepoImpl.openEpub(currentBook) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                    item = currentBook,
                                    highlightText = state.lastQuery
                                )
                            }
                            "remote"->{
                                BookItem(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    item = currentBook,
                                    icon = {
                                        IconButton(onClick = {
                                            viewModel.onEvent(SearchResultsEvent.OnClickDownloadBook(currentBook))
                                        }) {
                                            Icon(
                                                imageVector = Icons.Outlined.FileDownload,
                                                contentDescription = "download"
                                            )
                                        }
                                    },
                                    highlightText = state.lastQuery
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                    }
                }


            }
        }
    }



    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}
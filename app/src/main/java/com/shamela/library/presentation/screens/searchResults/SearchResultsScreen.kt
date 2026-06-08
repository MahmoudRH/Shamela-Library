package com.shamela.library.presentation.screens.searchResults


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shamela.apptheme.presentation.common.EmptyListScreen
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.common.SearchTopBar
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.presentation.common.BookDetailsBottomSheet
import com.shamela.library.presentation.common.BookItem
import com.shamela.library.presentation.common.DownloadIconButton
import com.shamela.library.presentation.common.SectionItem
import com.shamela.library.presentation.screens.LocalPaddingValues

@Composable
fun SearchResultsScreen(
    viewModel: SearchResultsViewModel = hiltViewModel(),
    navigateToSectionBooksScreen: (categoryName: String, type: String) -> Unit,
    navigateBack: () -> Unit,
) {
    val state = viewModel.searchResultsState.collectAsStateWithLifecycle().value
    val localPadding = LocalPaddingValues.current
    val focusRequester = FocusRequester()
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    Column(Modifier.fillMaxSize().padding(localPadding)) {
        SearchTopBar(
            onNavigateBack = navigateBack,
            hint = "بحث..",
            focusRequester = focusRequester,
            value = state.query,
            onValueChanged = { viewModel.onEvent((SearchResultsEvent.OnSearchQueryChanged(it))) },
            onClickClear = { viewModel.onEvent(SearchResultsEvent.ClearSearchQuery) },
            onClickSearch = { query -> viewModel.onEvent(SearchResultsEvent.Search(query)) },
        )
        EmptyListScreen(
            visibility = state.isListEmpty,
            text = "لم يتم العثور على أي نتائج..",
        )
        LoadingScreen(visibility = state.isLoading)
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
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                            item = it,
                            highlightText = state.lastQuery)
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                    }
                }else{
                    items(state.booksResultsList, key = {it.id}) { currentBook->
                        when (state.type){
                            "local"->{
                                BookItem(modifier = Modifier
                                    .clickable {
                                        Log.e("SearchResultsScreen", "Item Clicked: ${currentBook.title} ", )
                                        FilesBooksRepoImpl.openEpub(
                                            currentBook,
                                            onAddQuoteToFavorite = { quote ->
                                                viewModel.onEvent(
                                                    SearchResultsEvent.AddQuoteToFavorite(quote)
                                                )
                                            })
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                    item = currentBook,
                                    highlightText = state.lastQuery,
                                    onInfoClick = { selectedBook = currentBook }
                                )
                            }
                            "remote"->{
                                BookItem(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    item = currentBook,
                                    onInfoClick = { selectedBook = currentBook },
                                    icon = {
                                        DownloadIconButton(
                                            bookId = currentBook.id,
                                            downloadStatuses = state.downloadStatuses,
                                            downloadedBookIds = state.downloadedBookIds,
                                            onDownloadClick = {
                                                viewModel.onEvent(SearchResultsEvent.OnClickDownloadBook(currentBook))
                                            },
                                            onCancelClick = {
                                                viewModel.onEvent(SearchResultsEvent.OnClickCancelDownload(currentBook.id))
                                            },
                                        )
                                    },
                                    highlightText = state.lastQuery
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                    }
                }


            }
        }
    }

    selectedBook?.let { book ->
        BookDetailsBottomSheet(book = book, onDismiss = { selectedBook = null })
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}
package com.shamela.library.presentation.screens.download


import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shamela.library.presentation.common.BookItem
import com.shamela.library.presentation.common.CharacterHeader
import com.shamela.library.presentation.common.SectionItem
import com.shamela.library.presentation.navigation.Download
import com.shamela.library.presentation.screens.library.ViewType
import com.shamela.library.presentation.screens.library.ViewTypeSection
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel = hiltViewModel(),
    navigateToSectionBooksScreen: (categoryName: String, type: String) -> Unit,
    navigateToSearchResultsScreen: (categoryName: String, type: String) -> Unit,

    ) {
    val downloadState = viewModel.downloadState.collectAsState().value

    LaunchedEffect(key1 = Unit, block = {
        Download.buttons.onEach {
            if (it) {
                Log.e("Mah ", "DownloadScreen: Search is clicked")
                navigateToSearchResultsScreen("all", "remote")
            }
        }.launchIn(this)
    })
    LaunchedEffect(key1 = Unit, block ={
        when ( downloadState.viewType){
            ViewType.Sections -> viewModel.onEvent(DownloadEvent.LoadUserSections)
            ViewType.Books -> viewModel.onEvent(DownloadEvent.LoadUserBooks)
        }
    } )
    LazyColumn(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ViewTypeSection(
                modifier = Modifier,
                selectedViewType = downloadState.viewType
            ) { viewModel.onEvent(DownloadEvent.OnChangeViewType(it)) }
        }


        when (downloadState.viewType) {
            ViewType.Sections -> {
                items(downloadState.sections, key = { it.id }) {
                    SectionItem(modifier = Modifier
                        .clickable {
                            navigateToSectionBooksScreen(it.name, "remote")
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp), item = it)
                    Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                }
            }

            ViewType.Books -> {


                val booksList =
                    downloadState.books.sortedBy { it.title }.groupBy { it.title.first() }
                booksList.forEach { (initial, books) ->
                    stickyHeader {
                        CharacterHeader(
                            modifier = Modifier,
                            char = initial
                        )
                    }
                    items(books, key = { it.id }) {
                        BookItem(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            icon = {
                                IconButton(onClick = {
                                    viewModel.onEvent(DownloadEvent.OnClickDownloadBook(it))
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.FileDownload,
                                        contentDescription = "download"
                                    )
                                }
                            },
                            item = it
                        )
                        if (it != books.last()) {
                            Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                        }
                    }
                }
            }
        }
    }
}


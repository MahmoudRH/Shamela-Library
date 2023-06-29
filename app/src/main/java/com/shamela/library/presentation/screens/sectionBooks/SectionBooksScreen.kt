package com.shamela.library.presentation.screens.sectionBooks


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shamela.apptheme.common.DefaultTopBar
import com.shamela.apptheme.common.LoadingScreen
import com.shamela.library.presentation.common.BookItem

@Composable
fun SectionBooksScreen(
    viewModel: SectionBooksViewModel = hiltViewModel(),
    categoryName: String,
    navigateBack: () -> Unit,
    navigateToSearchResultsScreen: (categoryName: String, type: String) -> Unit,
) {
    val sectionBooksState = viewModel.sectionBooksState.collectAsState().value
    Column {
        DefaultTopBar(
            title = categoryName,
            onNavigateBack = navigateBack,
            actionIcon = Icons.Outlined.Search,
            onActionClick = {
                navigateToSearchResultsScreen(categoryName,sectionBooksState.type)
            })
        LazyColumn(
            Modifier
                .fillMaxSize()
                .clipToBounds(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(sectionBooksState.books.values.toList(), key = { it.id }) { currentBook ->
                when (sectionBooksState.type) {
                    "local" -> {
                        BookItem(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            item = currentBook
                        )
                    }

                    "remote" -> {
                        BookItem(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            icon = {
                                IconButton(onClick = {
                                    viewModel.onEvent(
                                        SectionBooksEvent.OnClickDownloadBook(
                                            currentBook
                                        )
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.FileDownload,
                                        contentDescription = "download"
                                    )
                                }
                            },
                            item = currentBook
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
            }
        }
    }
    LoadingScreen(visibility = sectionBooksState.isLoading)

    LaunchedEffect(Unit) {
        viewModel.onEvent(SectionBooksEvent.LoadBooks)
    }

}
package com.shamela.library.presentation.screens.sectionBooks


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.folioreader.ui.activity.folioActivity.book.BookEvent
import com.shamela.apptheme.presentation.common.DefaultTopBar
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.library.data.local.files.FilesBooksRepoImpl
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
        SectionTopBar(
            title = categoryName,
            onNavigateBack = navigateBack,
            onSearch = { navigateToSearchResultsScreen(categoryName, sectionBooksState.type) },
            onDownload = { viewModel.onEvent(SectionBooksEvent.OnClickDownloadSection) })

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
                            modifier = Modifier
                                .clickable {
                                    FilesBooksRepoImpl.openEpub(
                                        currentBook,
                                        onAddQuoteToFavorite = { quote ->
                                            viewModel.onEvent(
                                                SectionBooksEvent.AddQuoteToFavorite(quote)
                                            )
                                        })
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionTopBar(
    title: String,
    onDownload: () -> Unit,
    onSearch: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier,
        title = {
            var titleTextStyle by remember { mutableStateOf(AppFonts.textLargeBold) }
            var readyToDraw by remember { mutableStateOf(false) }
            Text(
                text = title,
                style = titleTextStyle,
                maxLines = 2,
                modifier = Modifier.drawWithContent { if (readyToDraw) drawContent() },
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.didOverflowHeight) {
                        titleTextStyle =
                            titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                    } else {
                        readyToDraw = true
                    }
                }
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(15.dp),
        ),
        actions = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Outlined.Search, contentDescription = null)
            }
            Spacer(modifier = Modifier.size(4.dp))
            IconButton(onClick = onDownload) {
                Icon(Icons.Outlined.FileDownload, contentDescription = null)
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowForwardIos, contentDescription = null)
            }
        }
    )
}
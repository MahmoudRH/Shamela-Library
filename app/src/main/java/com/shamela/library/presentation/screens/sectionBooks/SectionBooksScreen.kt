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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.ShamelaIcons
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.DownloadStatus
import com.shamela.library.domain.util.BookSortOption
import com.shamela.library.domain.util.BookSorter
import com.shamela.library.presentation.common.BookDetailsBottomSheet
import com.shamela.library.presentation.common.BookItem
import com.shamela.library.presentation.common.BookSortMenu
import com.shamela.library.presentation.common.DownloadIconButton

@Composable
fun SectionBooksScreen(
    viewModel: SectionBooksViewModel = hiltViewModel(),
    categoryName: String,
    navigateBack: () -> Unit,
    navigateToSearchResultsScreen: (categoryName: String, type: String) -> Unit,
) {
    val sectionBooksState = viewModel.sectionBooksState.collectAsStateWithLifecycle().value
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    // Download time only applies to local books (remote ones have no file on disk).
    val availableSortOptions = remember(sectionBooksState.type) {
        if (sectionBooksState.type == "local") BookSortOption.values().toList()
        else BookSortOption.values().filter { it != BookSortOption.DOWNLOAD_TIME }
    }
    val sortedBooks = remember(
        sectionBooksState.books,
        sectionBooksState.sortOption,
        sectionBooksState.sortAscending,
        sectionBooksState.downloadTimes,
    ) {
        BookSorter.sortBooks(
            books = sectionBooksState.books.values.toList(),
            option = sectionBooksState.sortOption,
            ascending = sectionBooksState.sortAscending,
            downloadTimes = sectionBooksState.downloadTimes,
        )
    }

    val downloadedInSection = remember(sectionBooksState.downloadedBookIds, sectionBooksState.books) {
        sectionBooksState.downloadedBookIds.intersect(sectionBooksState.books.keys).size
    }
    val totalInSection = sectionBooksState.books.size
    val hasActiveDownloadsInSection = remember(sectionBooksState.downloadStatuses, sectionBooksState.books) {
        sectionBooksState.books.keys.any { sectionBooksState.downloadStatuses[it] is DownloadStatus.Downloading }
    }

    Column {
        SectionTopBar(
            title = categoryName,
            onNavigateBack = navigateBack,
            onSearch = { navigateToSearchResultsScreen(categoryName, sectionBooksState.type) },
            onDownload = { viewModel.onEvent(SectionBooksEvent.OnClickDownloadSection) },
            isDownloadButtonEnabled = sectionBooksState.isDownloadButtonEnabled,
            downloadedBookCount = downloadedInSection,
            totalBookCount = totalInSection,
            hasActiveDownloads = hasActiveDownloadsInSection,
            sortOption = sectionBooksState.sortOption,
            sortAscending = sectionBooksState.sortAscending,
            availableSortOptions = availableSortOptions,
            onSortOptionSelected = { viewModel.onEvent(SectionBooksEvent.OnChangeSortOption(it)) },
            onToggleSortDirection = { viewModel.onEvent(SectionBooksEvent.OnToggleSortDirection) },
        )

        LazyColumn(
            Modifier
                .fillMaxSize()
                .clipToBounds(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(sortedBooks, key = { it.id }) { currentBook ->
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
                            item = currentBook,
                            onInfoClick = { selectedBook = currentBook }
                        )
                    }

                    "remote" -> {
                        BookItem(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            onInfoClick = { selectedBook = currentBook },
                            icon = {
                                DownloadIconButton(
                                    bookId = currentBook.id,
                                    downloadStatuses = sectionBooksState.downloadStatuses,
                                    downloadedBookIds = sectionBooksState.downloadedBookIds,
                                    onDownloadClick = {
                                        viewModel.onEvent(
                                            SectionBooksEvent.OnClickDownloadBook(currentBook)
                                        )
                                    },
                                    onCancelClick = {
                                        viewModel.onEvent(
                                            SectionBooksEvent.OnClickCancelDownload(currentBook.id)
                                        )
                                    },
                                )
                            },
                            item = currentBook
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
            }
        }
    }
    LoadingScreen(visibility = sectionBooksState.isLoading)

    selectedBook?.let { book ->
        BookDetailsBottomSheet(book = book, onDismiss = { selectedBook = null })
    }

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
    isDownloadButtonEnabled: Boolean,
    downloadedBookCount: Int,
    totalBookCount: Int,
    hasActiveDownloads: Boolean,
    sortOption: BookSortOption,
    sortAscending: Boolean,
    availableSortOptions: List<BookSortOption>,
    onSortOptionSelected: (BookSortOption) -> Unit,
    onToggleSortDirection: () -> Unit,
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
            BookSortMenu(
                sortOption = sortOption,
                ascending = sortAscending,
                availableOptions = availableSortOptions,
                onOptionSelected = onSortOptionSelected,
                onToggleDirection = onToggleSortDirection,
            )
            IconButton(onClick = onSearch) {
                Icon(ShamelaIcons.Search, contentDescription = null)
            }
            Spacer(modifier = Modifier.size(4.dp))
            IconButton(onClick = onDownload, enabled = isDownloadButtonEnabled) {
                when {
                    totalBookCount > 0 && downloadedBookCount == totalBookCount -> {
                        Icon(
                            ShamelaIcons.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    hasActiveDownloads -> {
                        CircularProgressIndicator(
                            progress = { if (totalBookCount > 0) downloadedBookCount.toFloat() / totalBookCount else 0f },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                        )
                    }
                    else -> {
                        Icon(ShamelaIcons.FileDownload, contentDescription = null)
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(ShamelaIcons.ArrowForwardIos, contentDescription = null)
            }
        }
    )
}

package com.shamela.library.presentation.screens.sectionBooks


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import com.shamela.library.presentation.common.BookItem
import com.shamela.library.presentation.common.DefaultTopBar
import com.shamela.library.presentation.common.LoadingScreen
import com.shamela.library.presentation.screens.download.DownloadEvent
import com.shamela.library.presentation.utils.BooksDownloadManager

@Composable
fun SectionBooksScreen(
    viewModel: SectionBooksViewModel = hiltViewModel(),
    categoryName: String,
    navigateBack: () -> Unit,
) {
    val sectionBooksState = viewModel.sectionBooksState.collectAsState().value
    Column {
        DefaultTopBar(title = categoryName, navigateBack)
        LazyColumn(
            Modifier
                .fillMaxSize()
                .clipToBounds(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(sectionBooksState.books) { currentBook ->
                BookItem(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    icon = {
                        IconButton(onClick = {
                            viewModel.onEvent(SectionBooksEvent.OnClickDownloadBook(currentBook))
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = "download"
                            )
                        }
                    },
                    item = currentBook
                )
                Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
            }
        }
    }
    LoadingScreen(visibility = sectionBooksState.isLoading || BooksDownloadManager.downloadIdMap.isNotEmpty())

    LaunchedEffect(Unit) {
        viewModel.onEvent(SectionBooksEvent.LoadBooks)
    }

}
package com.shamela.library.presentation.screens.library


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shamela.apptheme.common.LoadingScreen
import com.shamela.apptheme.theme.AppFonts
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.presentation.common.BookItem
import com.shamela.library.presentation.common.SectionItem


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    navigateToSectionBooksScreen: (categoryName: String, type: String) -> Unit,
) {
    val libraryState = viewModel.libraryState.collectAsState().value
    LazyColumn(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ViewTypeSection(
                modifier = Modifier,
                selectedViewType = libraryState.viewType
            ) { viewModel.onEvent(LibraryEvent.OnChangeViewType(it)) }
        }
        item {
            LoadingScreen(visibility = libraryState.isLoading)
        }
        when (libraryState.viewType) {
            ViewType.Sections -> {
                items(libraryState.sections.values.toList(), key = { it.id }) {
                    SectionItem(modifier = Modifier
                        .clickable {
                            navigateToSectionBooksScreen(it.name, "local")
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp), item = it)
                    Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                }
            }

            ViewType.Books -> {
                        items( libraryState.books.values.toList(), key = { it.id }) {
                            BookItem(modifier = Modifier
                                .clickable {
                                    FilesBooksRepoImpl.openEpub(it)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp), item = it)
                            Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                        }
            }
        }
    }
}

@Composable
fun ViewTypeSection(modifier: Modifier, selectedViewType: ViewType, onClick: (ViewType) -> Unit) {
    Row(
        modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 16.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
            .height(IntrinsicSize.Min)
    ) {
        ViewType.values().forEach {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selectedViewType == it) MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.4f
                        ) else Color.Transparent
                    )
                    .clickable { onClick(it) }
                    .padding(vertical = 12.dp),
                text = it.label,
                style = AppFonts.textNormalBold,
                textAlign = TextAlign.Center
            )
            if (it != ViewType.values().last()) {
                Box(
                    Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                )
            }
        }
    }
}



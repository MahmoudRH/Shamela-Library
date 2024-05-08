package com.shamela.library.presentation.screens.library


import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.presentation.common.LibraryBookItem
import com.shamela.library.presentation.common.SectionItem
import com.shamela.library.presentation.navigation.Library
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    navigateToSectionBooksScreen: (categoryName: String, type: String) -> Unit,
    navigateToSearchResultsScreen: (categoryName: String, type: String) -> Unit,
) {
    LaunchedEffect(key1 = Unit, block = {
        Library.buttons.onEach {
            if (it) {
                Log.e("Mah ", "LibraryScreen: Search is clicked")
                navigateToSearchResultsScreen("all", "local")
            }
        }.launchIn(this)
    })
    val libraryState = viewModel.libraryState.collectAsState().value
    LazyColumn(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ViewTypeSection(
                modifier = Modifier,
                selectedBooksViewType = libraryState.booksViewType
            ) { viewModel.onEvent(LibraryEvent.OnChangeViewType(it)) }
        }
        item {
            LoadingScreen(visibility = libraryState.isLoading)
        }
        when (libraryState.booksViewType) {
            BooksViewType.Sections -> {
                items(libraryState.sections.values.toList(), key = { it.id }) {
                    SectionItem(modifier = Modifier
                        .clickable {
                            navigateToSectionBooksScreen(it.name, "local")
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp), item = it)
                    Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                }
            }

            BooksViewType.Books -> {
                item {
                    AnimatedVisibility(visible = libraryState.selectedBooks.isNotEmpty(), enter = expandVertically(), exit = shrinkVertically()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.onEvent(LibraryEvent.DeleteSelectedBooks)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "delete"
                                )
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "حذف الكتب المحددة",
                                    style = AppFonts.textNormal,
                                )
                            }
                            Button(onClick = {
                                viewModel.onEvent(LibraryEvent.CancelSelection)
                            }) {
                                Text(text = "إلغاء", style = AppFonts.textNormal, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }

                items(libraryState.books.values.toList(), key = { it.id }) {
                    LibraryBookItem(modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { _ ->
                                if (viewModel.libraryState.value.selectedBooks.isEmpty()) {
                                    FilesBooksRepoImpl.openEpub(
                                        it,
                                        onAddQuoteToFavorite = { quote ->
                                            viewModel.onEvent(
                                                LibraryEvent.AddQuoteToFavorite(
                                                    quote
                                                )
                                            )
                                        })
                                } else {
                                    viewModel.onEvent(LibraryEvent.SelectBook(it))
                                }
                            }, onLongPress = { _ ->
                                viewModel.onEvent(LibraryEvent.SelectBook(it))
                            })
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .animateItemPlacement(),
                        item = it,
                        onFavoriteIconClicked = { viewModel.onEvent(LibraryEvent.ToggleFavorite(it)) },
                        onSwipeOut = {
                            viewModel.onEvent(LibraryEvent.DeleteBook(it))
                        },
                        isSelected = libraryState.selectedBooks.contains(it)
                    )
                    Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                }
            }
        }
    }
}

@Composable
fun ViewTypeSection(
    modifier: Modifier,
    selectedBooksViewType: BooksViewType,
    onClick: (BooksViewType) -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 16.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
            .height(IntrinsicSize.Min)
    ) {
        BooksViewType.values().forEach {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selectedBooksViewType == it) MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.4f
                        ) else Color.Transparent
                    )
                    .clickable { onClick(it) }
                    .padding(vertical = 12.dp),
                text = it.label,
                style = AppFonts.textNormalBold,
                textAlign = TextAlign.Center
            )
            if (it != BooksViewType.values().last()) {
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



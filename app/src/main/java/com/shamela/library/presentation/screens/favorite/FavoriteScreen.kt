package com.shamela.library.presentation.screens.favorite


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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shamela.apptheme.presentation.common.EmptyListScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.presentation.common.LocalBookItem
import com.shamela.library.presentation.common.QuoteItem
import com.shamela.library.presentation.common.StringHeader
import com.shamela.library.presentation.screens.library.LibraryEvent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel = hiltViewModel(),
) {
    val state = viewModel.favoriteState.collectAsState().value

    LaunchedEffect(Unit){
        viewModel.onEvent(FavoriteEvent.LoadFavoriteQuotes)
    }
    LazyColumn(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ViewTypeSection(
                modifier = Modifier,
                selectedFavoriteViewType = state.viewType
            ) { viewModel.onEvent(FavoriteEvent.OnChangeViewType(it)) }
        }
        when(state.viewType){
            FavoriteViewType.Quotes -> {
                val quotesMap = state.favoriteQuotes.groupBy { it.bookName }
                quotesMap.forEach{ (bookName, quotes) ->
                    stickyHeader {
                        StringHeader(
                            modifier = Modifier,
                            bookName = bookName
                        )
                    }
                    items(quotes, key = {it.quoteId}){currentQuote->
                        QuoteItem(
                            modifier = Modifier
                                .clickable {
                                    viewModel.onEvent(FavoriteEvent.OpenBookForQuote(currentQuote))
                                }.padding(horizontal = 16.dp, vertical = 8.dp)
                                .animateItemPlacement(),
                            item = currentQuote)
                        Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                    }

                }
                item {
                    EmptyListScreen(
                        visibility = state.isListEmpty,
                        text = "مفضلة الإقتباسات فارغة،\n أضف بعض الإقتباسات للمفضلة!",
                        modifier = Modifier.fillParentMaxSize()
                    )
                }
            }
            FavoriteViewType.Books -> {
                items(state.favoriteBooks, key = { it.id }) { currentBook ->
                    LocalBookItem(
                        modifier = Modifier
                            .clickable {
                                FilesBooksRepoImpl.openEpub(
                                    currentBook,
                                    onAddQuoteToFavorite = {quote ->
                                        viewModel.onEvent(FavoriteEvent.AddQuoteToFavorite(quote))
                                    })
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .animateItemPlacement(),
                        onFavoriteIconClicked = {
                            viewModel.onEvent(FavoriteEvent.ToggleFavorite(currentBook))
                        },
                        item = currentBook,
                    )
                    Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                }
                item {
                    EmptyListScreen(
                        visibility = state.isListEmpty,
                        text = "مفضلة الكتب فارغة،\n أضف بعض الكتب للمفضلة!",
                        modifier = Modifier.fillParentMaxSize())
                }
            }
        }

    }
}

@Composable
private fun ViewTypeSection(modifier: Modifier, selectedFavoriteViewType: FavoriteViewType, onClick: (FavoriteViewType) -> Unit) {
    Row(
        modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 16.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
            .height(IntrinsicSize.Min)
    ) {
        FavoriteViewType.values().forEach {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selectedFavoriteViewType == it) MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.4f
                        ) else Color.Transparent
                    )
                    .clickable { onClick(it) }
                    .padding(vertical = 12.dp),
                text = it.label,
                style = AppFonts.textNormalBold,
                textAlign = TextAlign.Center
            )
            if (it != FavoriteViewType.values().last()) {
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

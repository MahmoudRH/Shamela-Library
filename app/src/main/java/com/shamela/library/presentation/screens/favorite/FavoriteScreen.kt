package com.shamela.library.presentation.screens.favorite


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shamela.apptheme.presentation.common.EmptyListScreen
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.presentation.common.LocalBookItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel = hiltViewModel(),
) {
    val state = viewModel.favoriteState.collectAsState().value

    LaunchedEffect(key1 = Unit, block = {
        viewModel.onEvent(FavoriteEvent.LoadFavoriteBooks)
    })
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.favoriteBooks, key = { it.id }) { currentBook ->
            LocalBookItem(
                modifier = Modifier
                    .clickable { FilesBooksRepoImpl.openEpub(currentBook) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .animateItemPlacement(),
                onFavoriteIconClicked = {
                    viewModel.onEvent(FavoriteEvent.ToggleFavorite(currentBook))
                },
                item = currentBook,
            )
            Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
        }
    }
    EmptyListScreen(visibility = state.isListEmpty, text = "المفضلة فارغة، أضف بعض الكتب!")
}
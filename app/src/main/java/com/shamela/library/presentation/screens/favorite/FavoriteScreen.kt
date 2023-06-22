package com.shamela.library.presentation.screens.favorite


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shamela.apptheme.theme.AppFonts

@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel = hiltViewModel(),
){
val favoriteState = viewModel.favoriteState.collectAsState().value
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "المفضلة", style = AppFonts.textNormal)
    }
}
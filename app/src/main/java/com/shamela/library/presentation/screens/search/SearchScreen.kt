package com.shamela.library.presentation.screens.search


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.shamela.apptheme.presentation.theme.AppFonts

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
){
val searchState = viewModel.searchState.collectAsState().value
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "البحث", style = AppFonts.textNormal)
    }
}
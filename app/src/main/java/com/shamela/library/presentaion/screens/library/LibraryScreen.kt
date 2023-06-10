package com.shamela.library.presentaion.screens.library


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shamela.library.presentaion.theme.alMajeedPlain

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
){
val libraryState = viewModel.libraryState.collectAsState().value
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "المكتبة", style = alMajeedPlain)
    }
}
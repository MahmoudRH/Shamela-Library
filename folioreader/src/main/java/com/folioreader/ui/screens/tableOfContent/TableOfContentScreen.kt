package com.folioreader.ui.screens.tableOfContent


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
fun TableOfContentScreen(
    viewModel: TableOfContentViewModel = hiltViewModel(),
){
val tableOfContentState = viewModel.tableOfContentState.collectAsState().value
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "أقسام الكتاب", style = AppFonts.textNormal)
    }
}
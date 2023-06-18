package com.shamela.library.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.shamela.library.presentation.theme.AppFonts

@Composable
fun CharacterHeader(modifier: Modifier, char: Char) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary
                    .copy(0.1f)
                    .compositeOver(MaterialTheme.colorScheme.background)
            )
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = char.toString(),
            style = AppFonts.textNormalBold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}
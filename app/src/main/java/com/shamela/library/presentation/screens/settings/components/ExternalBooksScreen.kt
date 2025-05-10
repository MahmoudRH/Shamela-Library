package com.shamela.library.presentation.screens.settings.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import com.shamela.apptheme.presentation.theme.colors.Green
import com.shamela.apptheme.presentation.util.ShamelaPrev
import com.shamela.library.R

@Composable
fun ColumnScope.ExternalBooksScreen(
    onClickSelectBook: () -> Unit,
    onClickAddBookToLibrary: () -> Unit,
    selectedFileName: String?,
    selectedFileUri: Uri?
) {
    Text(
        text = stringResource(R.string.add_external_book), modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp), style = AppFonts.textNormalBold
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
    SelectBookButton(selectedFileName, onClickSelectBook)

    AnimatedVisibility(visible = selectedFileUri != null) {
        Button(onClick = onClickAddBookToLibrary) {
            Text(
                text = stringResource(R.string.add_to_library),
                style = AppFonts.textNormal
            )
        }
    }
}

@Composable
private fun SelectBookButton(
    selectedFileName: String?,
    onClickSelectBook: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .clickable(onClick = onClickSelectBook)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier,
            text = selectedFileName ?: stringResource(R.string.select_book),
            style = AppFonts.textNormal
        )
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}


@ShamelaPrev
@Composable
private fun ExternalBooksScreenPrev() {
    AppTheme.ShamelaLibraryTheme {
        AppTheme.changeColorScheme(Green.lightColorScheme, Green.name)
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ExternalBooksScreen(
                onClickSelectBook = { },
                onClickAddBookToLibrary = {},
                selectedFileName = stringResource(R.string.select_book),
                selectedFileUri = null,
            )
        }
    }
}
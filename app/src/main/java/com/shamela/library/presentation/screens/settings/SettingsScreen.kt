package com.shamela.library.presentation.screens.settings


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.settings.PreferenceSettingsScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.library.presentation.screens.LocalPaddingValues
import com.shamela.library.presentation.screens.settings.components.ExternalBooksScreen


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.settingsState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onEvent(SettingsEvent.NewFileSelected(uri))
            }
        })
    LaunchedEffect(key1 = Unit) {
        viewModel.toastsChannel.collect { stringRes ->
            Toast.makeText(context, context.getString(stringRes), Toast.LENGTH_SHORT).show()
        }
    }

    SettingsScreenUI(uiState, {
        getContentLauncher.launch("application/epub+zip")
    }, { viewModel.onEvent(SettingsEvent.OnChangeViewType(it)) }) {
        uiState.fileUri?.let { uri ->
            uiState.fileName?.let {
                viewModel.onEvent(
                    SettingsEvent.AddExternalBookToLibrary(uri, uiState.fileName)
                )
            }
        }
    }
}

@Composable
private fun SettingsScreenUI(
    uiState: SettingsState,
    onClickSelectBook: () -> Unit,
    onChangeViewType: (SettingsViewType) -> Unit,
    onClickAddBookToLibrary: () -> Unit
) {
    val localPadding = LocalPaddingValues.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(localPadding)
    ) {
        ViewTypeSection(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            selectedViewType = uiState.selectedViewType,
            onClick = onChangeViewType
        )
        LoadingScreen(visibility = uiState.isLoading)
        when (uiState.selectedViewType) {
            SettingsViewType.Preferences -> PreferenceSettingsScreen()
            SettingsViewType.ExternalBooks -> ExternalBooksScreen(
                onClickSelectBook = onClickSelectBook,
                onClickAddBookToLibrary = onClickAddBookToLibrary,
                selectedFileName = uiState.fileName,
                selectedFileUri = uiState.fileUri
            )

        }
    }
}

@Composable
private fun ViewTypeSection(
    modifier: Modifier,
    selectedViewType: SettingsViewType,
    onClick: (SettingsViewType) -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 16.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
            .height(IntrinsicSize.Min)
    ) {
        SettingsViewType.entries.forEach {
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
                text = stringResource(it.label),
                style = AppFonts.textNormalBold,
                textAlign = TextAlign.Center
            )
            if (it != SettingsViewType.entries.last()) {
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


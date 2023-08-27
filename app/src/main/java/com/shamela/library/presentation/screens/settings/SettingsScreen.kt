package com.shamela.library.presentation.screens.settings


import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.settings.PreferenceSettingsScreen
import com.shamela.apptheme.presentation.theme.AppFonts


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settingsState = viewModel.settingsState.collectAsState().value
    val context = LocalContext.current
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                Log.e("SettingsScreen", "new uri selected: $uri")
                viewModel.onEvent(SettingsEvent.NewFileSelected(uri))
            }
        })

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ViewTypeSection(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            selectedViewType = settingsState.selectedViewType
        ) { viewModel.onEvent(SettingsEvent.OnChangeViewType(it)) }
        LoadingScreen(visibility = settingsState.isLoading)
        when (settingsState.selectedViewType) {
            SettingsViewType.Preferences -> PreferenceSettingsScreen()
            SettingsViewType.ExternalBooks -> {
                Text(
                    text = "إضافة كتاب خارجي", modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp), style = AppFonts.textNormalBold
                )
                Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                        .clickable {
                            getContentLauncher.launch("application/epub+zip")
                        }
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                    ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier,
                        text = settingsState.fileName,
                        style = AppFonts.textNormal
                    )
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)

                }

                AnimatedVisibility(visible = settingsState.fileUri != null) {
                    Button(onClick = {
                        settingsState.fileUri?.let { uri ->
                            viewModel.onEvent(
                                SettingsEvent.AddExternalBookToLibrary(uri, settingsState.fileName)
                            )
                        }
                    }) {
                        Text(
                            text = "إضافة إلى المكتبة",
                            style = AppFonts.textNormal
                        )
                    }
                }
            }
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
        SettingsViewType.values().forEach {
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
                text = it.label,
                style = AppFonts.textNormalBold,
                textAlign = TextAlign.Center
            )
            if (it != SettingsViewType.values().last()) {
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


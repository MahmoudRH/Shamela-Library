package com.shamela.library.presentation.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shamela.apptheme.presentation.common.DefaultTopBar
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.library.R

@Composable
fun AboutAppScreen(
    viewModel: AboutAppViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            DefaultTopBar(
                title = "حول التطبيق",
                onNavigateBack = navigateBack,
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFB08D57)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Unspecified,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "المكتبة الشاملة",
                    style = AppFonts.textLargeBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "تطبيق لقراءة الكتب الإسلامية بصيغة EPUB، يتيح لك تحميل الكتب وتصفّحها في أي وقت دون الحاجة إلى اتصال بالإنترنت.",
                    style = AppFonts.textNormal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.3f))
                Spacer(Modifier.height(24.dp))
            }

            item {
                VersionSection(state = state)
                Spacer(Modifier.height(16.dp))
                if (state.updateAvailable && !state.isDownloading) {
                    Button(
                        onClick = { viewModel.onEvent(AboutAppEvent.DownloadAndInstall) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("تحديث التطبيق", style = AppFonts.textNormalBold)
                    }
                }
                if (state.isDownloading) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "جارٍ التحميل… ${state.downloadProgress}%",
                            style = AppFonts.textNormal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                state.error?.let { err ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = err,
                        style = AppFonts.textNormal,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            if (state.releaseNotes.isNotEmpty()) {
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "ما الجديد",
                        style = AppFonts.textLargeBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.releaseNotes,
                        style = AppFonts.textNormal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun VersionSection(state: AboutAppState) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            VersionRow(label = "الإصدار الحالي", version = state.currentVersion)
            Spacer(Modifier.height(8.dp))
            if (state.isLoadingLatestVersion) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("أحدث إصدار: ", style = AppFonts.textNormal, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                }
            } else if (state.latestVersion.isNotEmpty()) {
                VersionRow(
                    label = "أحدث إصدار",
                    version = state.latestVersion,
                    highlight = state.updateAvailable,
                )
            }
        }
    }
}

@Composable
private fun VersionRow(label: String, version: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AppFonts.textNormal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = version,
            style = AppFonts.textNormalBold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
        )
    }
}

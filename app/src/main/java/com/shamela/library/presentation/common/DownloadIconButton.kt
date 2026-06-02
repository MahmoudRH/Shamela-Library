package com.shamela.library.presentation.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shamela.apptheme.presentation.theme.ShamelaIcons
import com.shamela.library.domain.model.DownloadStatus

@Composable
fun DownloadIconButton(
    bookId: String,
    downloadStatuses: Map<String, DownloadStatus>,
    downloadedBookIds: Set<String>,
    onDownloadClick: () -> Unit,
    onCancelClick: () -> Unit = {},
) {
    val status: DownloadStatus = when {
        downloadedBookIds.contains(bookId) -> DownloadStatus.Downloaded
        downloadStatuses.containsKey(bookId) -> downloadStatuses[bookId]!!
        else -> DownloadStatus.NotDownloaded
    }

    when (status) {
        DownloadStatus.NotDownloaded -> {
            IconButton(onClick = onDownloadClick) {
                Icon(imageVector = ShamelaIcons.FileDownload, contentDescription = "تحميل")
            }
        }
        is DownloadStatus.Downloading -> {
            IconButton(onClick = onCancelClick) {
                if (status.progress > 0) {
                    CircularProgressIndicator(
                        progress = { status.progress / 100f },
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                    )
                }
            }
        }
        DownloadStatus.Downloaded -> {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = ShamelaIcons.CheckCircle,
                    contentDescription = "تم التحميل",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

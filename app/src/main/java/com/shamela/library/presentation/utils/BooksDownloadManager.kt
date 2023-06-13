package com.shamela.library.presentation.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.shamela.library.domain.model.Book
import kotlinx.coroutines.flow.StateFlow

class BooksDownloadManager(private val context: Context) {

    companion object {
        private val _downloadIdMap = mutableStateMapOf<Long, Book>()
        fun downloadIsDone(downloadId: Long) {
            if (_downloadIdMap.contains(downloadId)) {
                _downloadIdMap.remove(downloadId)
            }
        }

        val downloadIdMap by derivedStateOf { _downloadIdMap }
    }

    private val downManager =
        context.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager


    fun downloadBook(downloadUri: Uri, book: Book, bookCategory: String): Long {
        val bookTitle = book.title
        val request = DownloadManager.Request(downloadUri)
        request.setTitle("المكتبة الشاملة")
        request.setDescription("جار تحميل كتاب ($bookTitle)")
        request.setMimeType("application/epub+zip")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "ShamelaDownloads/$bookCategory/$bookTitle.epub"
        )
        val downloadId = downManager.enqueue(request)
        _downloadIdMap[downloadId] = book
        return downloadId
    }

    fun cancelBookDownload(downloadId: Long) {
        val book = _downloadIdMap[downloadId] ?: return
        downManager.remove(downloadId)
        _downloadIdMap.remove(downloadId)
    }

}


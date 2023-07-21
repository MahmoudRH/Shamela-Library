package com.shamela.library.presentation.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import com.shamela.library.ShamelaApp
import androidx.compose.runtime.mutableStateMapOf
import com.shamela.library.domain.model.Book
import java.io.File

class BooksDownloadManager(private val context: Context) {

    companion object {
        private val _downloadIdMap = mutableStateMapOf<Long, Book>()
        private val subscribers: MutableList<Subscriber> = mutableListOf()
        const val TAG = "BooksDownloadManager"
        const val FILE_ALREADY_EXISTS = -1L
        fun subscribe(subscriber: Subscriber) {
            if (!subscribers.contains(subscriber)) {
                subscribers.add(subscriber)
            }
        }

        fun unsubscribe(subscriber: Subscriber) {
            if (subscribers.contains(subscriber)) {
                subscribers.remove(subscriber)
            }
        }

        fun downloadIsDone(downloadId: Long, saveDownloadedBook: (Book) -> Unit) {
            if (_downloadIdMap.contains(downloadId)) {
                 val temp =  _downloadIdMap[downloadId]
                  _downloadIdMap.remove(downloadId)
                temp?.let {
                    saveDownloadedBook(it)
                    Log.e(TAG, "downloadIsDone: Book = $it ", )
                    Log.e(TAG, "downloadIsDone: _downloadIdMap.size = ${_downloadIdMap.size-1} ", )
                }
                subscribers.forEach {
                    it.onBookDownloaded(temp!!, _downloadIdMap.isEmpty())
                }

            }
        }
    }

    private val downManager =
        context.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager


    fun downloadBook(downloadUri: Uri, book: Book, bookCategory: String): Long {
        val bookTitle = book.title
        val downloadsFolder = ShamelaApp.externalMediaDir
        val bookFileSubPath = "ShamelaDownloads/$bookCategory/$bookTitle.epub"
        val isFileAlreadyDownloaded = File(downloadsFolder,bookFileSubPath).isFile
        if (isFileAlreadyDownloaded || _downloadIdMap.values.contains(book)) return FILE_ALREADY_EXISTS
        val request = DownloadManager.Request(downloadUri)
        request.setTitle("المكتبة الشاملة")
        request.setDescription("جار تحميل كتاب ($bookTitle)")
        request.setMimeType("application/epub+zip")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val destinationFile = File(downloadsFolder,bookFileSubPath)
        request.setDestinationUri(Uri.fromFile(destinationFile))
        val downloadId = downManager.enqueue(request)
        _downloadIdMap[downloadId] = book
        return downloadId
    }

    fun downloadSection(booksMap: Map<Book, Uri?>){
        Log.e(TAG, "booksMap: ${booksMap.values}", )
        booksMap.forEach { (book, uri) ->
            if (uri!=null){
                val bookTitle = book.title
                val downloadsFolder = ShamelaApp.externalMediaDir
                val bookFileSubPath = "ShamelaDownloads/${book.categoryName}/$bookTitle.epub"
                if(!File(downloadsFolder,bookFileSubPath).isFile){
                    val request = DownloadManager.Request(uri)
                    request.setTitle("المكتبة الشاملة")
                    request.setDescription("جار تحميل كتاب ($bookTitle)")
                    request.setMimeType("application/epub+zip")
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    val destinationFile = File(downloadsFolder,bookFileSubPath)
                    request.setDestinationUri(Uri.fromFile(destinationFile))
                    val downloadId = downManager.enqueue(request)
                    Log.e(TAG, "enqueued downloadId: $downloadId ", )
                    _downloadIdMap[downloadId] = book
                }
            }
        }
    }



    fun cancelBookDownload(downloadId: Long) {
        val book = _downloadIdMap[downloadId] ?: return
        downManager.remove(downloadId)
        _downloadIdMap.remove(downloadId)
    }

    interface Subscriber {
        fun onBookDownloaded(book: Book, isLastBook:Boolean)
    }
}




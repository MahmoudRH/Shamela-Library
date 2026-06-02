package com.shamela.library.presentation.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.shamela.apptheme.presentation.worker.BookPreparationWorker
import com.shamela.library.ShamelaApp
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BooksDownloadManager(context: Context) {

    init {
        initializeWith(context)
    }

    companion object {
        private val _downloadIdMap = ConcurrentHashMap<Long, Book>()
        private val _bookIdToDownloadId = ConcurrentHashMap<String, Long>()
        private val _statusMap = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
        val downloadStatusFlow: StateFlow<Map<String, DownloadStatus>> = _statusMap.asStateFlow()
        private val pollerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private var pollerJob: Job? = null
        private val subscribers: MutableList<Subscriber> = mutableListOf()
        private lateinit var downManager: DownloadManager
        const val TAG = "BooksDownloadManager"
        const val FILE_ALREADY_EXISTS = -1L

        private fun initializeWith(context: Context) {
            if (!::downManager.isInitialized) {
                downManager = context.applicationContext
                    .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            }
        }

        fun subscribe(subscriber: Subscriber) {
            if (!subscribers.contains(subscriber)) subscribers.add(subscriber)
        }

        fun unsubscribe(subscriber: Subscriber) {
            subscribers.remove(subscriber)
        }

        fun downloadIsDone(downloadId: Long, saveDownloadedBook: (Book) -> Unit) {
            val book = _downloadIdMap.remove(downloadId) ?: return
            _bookIdToDownloadId.remove(book.id)
            // Do NOT update _statusMap here. The Downloading entry stays until Room confirms
            // the insert via the combine collector in each ViewModel (clearStatus). This avoids
            // the stale Downloaded entry that would persist through library deletions.
            saveDownloadedBook(book)
            Log.d(TAG, "downloadIsDone: book=${book.title}, remaining=${_downloadIdMap.size}")
            subscribers.forEach { it.onBookDownloaded(book, _downloadIdMap.isEmpty()) }
        }

        fun clearStatus(bookId: String) {
            updateStatus(bookId, DownloadStatus.NotDownloaded)
        }

        fun reconcileOnReceive(
            downloadId: Long,
            context: Context,
            workManager: WorkManager,
            saveBook: suspend (Book) -> Unit,
            scope: CoroutineScope,
        ) {
            if (_downloadIdMap.containsKey(downloadId)) return
            val dm = context.applicationContext
                .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val cursor = dm.query(DownloadManager.Query().setFilterById(downloadId))
            cursor.use {
                if (!it.moveToFirst()) return
                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val localUri = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                if (status != DownloadManager.STATUS_SUCCESSFUL || localUri == null) return
                val file = File(Uri.parse(localUri).path ?: return)
                val bookTitle = file.nameWithoutExtension
                val categoryName = file.parentFile?.name ?: return
                val bookId = UUID.nameUUIDFromBytes((bookTitle + categoryName).toByteArray()).toString()
                val book = Book(
                    id = bookId,
                    title = bookTitle,
                    author = "",
                    pageCount = 0,
                    categoryName = categoryName
                )
                scope.launch(Dispatchers.IO) {
                    saveBook(book)
                }
                workManager.enqueue(
                    OneTimeWorkRequestBuilder<BookPreparationWorker>()
                        .setInputData(workDataOf(BookPreparationWorker.EPUB_FILE_PATH to file.absolutePath))
                        .build()
                )
            }
        }

        fun getBookPath(book: Book): String {
            val downloadsFolder = ShamelaApp.externalMediaDir
            val bookFileSubPath = "ShamelaDownloads/${book.categoryName}/${book.title}.epub"
            return File(downloadsFolder, bookFileSubPath).absolutePath
        }

        fun cancelDownload(bookId: String) {
            val downloadId = _bookIdToDownloadId[bookId] ?: return
            _downloadIdMap.remove(downloadId)
            _bookIdToDownloadId.remove(bookId)
            downManager.remove(downloadId)
            updateStatus(bookId, DownloadStatus.NotDownloaded)
        }

        private fun updateStatus(bookId: String, status: DownloadStatus) {
            _statusMap.update { current ->
                if (status == DownloadStatus.NotDownloaded) current - bookId
                else current + (bookId to status)
            }
        }

        private fun ensurePollerRunning() {
            if (pollerJob?.isActive == true) return
            pollerJob = pollerScope.launch {
                while (_downloadIdMap.isNotEmpty()) {
                    val ids = _downloadIdMap.keys.toLongArray()
                    if (ids.isEmpty()) break
                    val cursor = downManager.query(DownloadManager.Query().setFilterById(*ids))
                    cursor.use {
                        while (it.moveToNext()) {
                            val dlId = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                            val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                            val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            val book = _downloadIdMap[dlId] ?: continue
                            val progress = if (total > 0) ((downloaded * 100) / total).toInt() else 0

                            when (status) {
                                DownloadManager.STATUS_RUNNING,
                                DownloadManager.STATUS_PENDING ->
                                    updateStatus(book.id, DownloadStatus.Downloading(progress))

                                DownloadManager.STATUS_SUCCESSFUL ->
                                    updateStatus(book.id, DownloadStatus.Downloading(100))

                                DownloadManager.STATUS_FAILED -> {
                                    _downloadIdMap.remove(dlId)
                                    _bookIdToDownloadId.remove(book.id)
                                    updateStatus(book.id, DownloadStatus.NotDownloaded)
                                }
                            }
                        }
                    }
                    delay(500)
                }
            }
        }
    }

    fun downloadBook(downloadUri: Uri, book: Book, bookCategory: String): Long {
        val bookTitle = book.title
        val downloadsFolder = ShamelaApp.externalMediaDir
        val bookFileSubPath = "ShamelaDownloads/$bookCategory/$bookTitle.epub"
        val isFileAlreadyDownloaded = File(downloadsFolder, bookFileSubPath).isFile
        if (isFileAlreadyDownloaded || _downloadIdMap.values.contains(book)) return FILE_ALREADY_EXISTS
        val request = DownloadManager.Request(downloadUri)
        request.setTitle("المكتبة الشاملة")
        request.setDescription("جار تحميل كتاب ($bookTitle)")
        request.setMimeType("application/epub+zip")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val destinationFile = File(downloadsFolder, bookFileSubPath)
        request.setDestinationUri(Uri.fromFile(destinationFile))
        val downloadId = downManager.enqueue(request)
        _downloadIdMap[downloadId] = book
        _bookIdToDownloadId[book.id] = downloadId
        updateStatus(book.id, DownloadStatus.Downloading(0))
        ensurePollerRunning()
        return downloadId
    }

    fun downloadSection(booksMap: Map<Book, Uri?>) {
        Log.d(TAG, "downloadSection: ${booksMap.size} books")
        booksMap.forEach { (book, uri) ->
            if (uri != null) {
                val bookTitle = book.title
                val downloadsFolder = ShamelaApp.externalMediaDir
                val bookFileSubPath = "ShamelaDownloads/${book.categoryName}/$bookTitle.epub"
                if (!File(downloadsFolder, bookFileSubPath).isFile) {
                    val request = DownloadManager.Request(uri)
                    request.setTitle("المكتبة الشاملة")
                    request.setDescription("جار تحميل كتاب ($bookTitle)")
                    request.setMimeType("application/epub+zip")
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    val destinationFile = File(downloadsFolder, bookFileSubPath)
                    request.setDestinationUri(Uri.fromFile(destinationFile))
                    val downloadId = downManager.enqueue(request)
                    Log.d(TAG, "enqueued downloadId=$downloadId for ${book.title}")
                    _downloadIdMap[downloadId] = book
                    _bookIdToDownloadId[book.id] = downloadId
                    updateStatus(book.id, DownloadStatus.Downloading(0))
                }
            }
        }
        ensurePollerRunning()
    }

    fun cancelBookDownload(downloadId: Long) {
        val book = _downloadIdMap.remove(downloadId) ?: return
        _bookIdToDownloadId.remove(book.id)
        downManager.remove(downloadId)
        updateStatus(book.id, DownloadStatus.NotDownloaded)
    }

    interface Subscriber {
        fun onBookDownloaded(book: Book, isLastBook: Boolean)
    }
}

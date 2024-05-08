package com.shamela.library.presentation.reciever

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.shamela.apptheme.presentation.worker.BookPreparationWorker
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadCompleteReceiver : BroadcastReceiver() {

    @Inject
    @FilesRepoImpl
    lateinit var booksUseCases: BooksUseCases

    private lateinit var workManager: WorkManager
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
            intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1).let { downloadId ->
                if (downloadId != -1L) {
                    BooksDownloadManager.downloadIsDone(downloadId) { book ->
                        CoroutineScope(Dispatchers.IO).launch {
                            booksUseCases.saveDownloadedBook(book)
                        }
                        workManager = WorkManager.getInstance(context)
                        val bookFilePath = BooksDownloadManager.getBookPath(book)
                        val request = OneTimeWorkRequestBuilder<BookPreparationWorker>()
                            .setInputData(
                                workDataOf(BookPreparationWorker.EPUB_FILE_PATH to bookFilePath)
                            )
                            .build()
                        workManager.enqueue(request)
                    }
                }
            }
        }
    }
}
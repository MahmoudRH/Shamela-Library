package com.shamela.library.presentation.reciever

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            BooksDownloadManager.downloadIsDone(downloadId){
                CoroutineScope(Dispatchers.IO).launch {
                    booksUseCases.saveDownloadedBook(it)
                }
            }
        }
    }
}
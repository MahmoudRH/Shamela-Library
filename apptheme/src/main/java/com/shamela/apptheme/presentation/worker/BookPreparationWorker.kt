package com.shamela.apptheme.presentation.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.shamela.apptheme.R
import com.shamela.apptheme.data.db.DatabaseHelper
import com.shamela.apptheme.domain.model.BookPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser.xmlParser
import org.readium.r2.streamer.parser.EpubParser
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


class BookPreparationWorker(
    private val appContext: Context,
    private val params: WorkerParameters,
) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val notification = startNotification(
            title = "تحضير الكتب للبحث",
            content = "يتم تحضير الكتب للبحث"
        )
        val notificationId = this.id.hashCode()
        val foreground = ForegroundInfo(notificationId, notification)
        setForeground(foreground)

        return withContext(Dispatchers.IO) {
            val bookFilePath = params.inputData.getString(EPUB_FILE_PATH)
                ?: return@withContext Result.failure()
            val database = DatabaseHelper(appContext)
            getPages(bookFilePath).forEach { (href, pageData) ->
                val tokens = bookFilePath.split("/")
                val bookTitle = tokens[tokens.lastIndex].removeSuffix(".epub")
                val category = tokens[tokens.lastIndex - 1]
                val uuidName = bookTitle + category
                val bookID = UUID.nameUUIDFromBytes(uuidName.toByteArray()).toString()
                val page = BookPage(
                    href = href,
                    content = pageData,
                    bookId = bookID,
                    category = category,
                    bookTitle = bookTitle
                )
                Log.e(TAG, "doWork: pageParsed: $page")

               val isSuccess = database.insertBookPage(page)
                Log.e(TAG, "doWork: pageInserted $isSuccess")

            }
            database.close()
            return@withContext Result.success()
        }
    }

    private suspend fun getPages(bookFilePath: String): Map<String, String> {
        return withContext(Dispatchers.IO) {
            val hrefs =
                EpubParser().parse(bookFilePath)?.publication?.readingOrder?.mapNotNull { it.href }
                    ?: emptyList()
            try {
                val pagesMap = mutableMapOf<String, String>()
                ZipFile(File(bookFilePath)).use { zipFile ->
                    hrefs.forEach { href ->
                        val entry = ZipEntry(href.removePrefix("/"))
                        Log.e(TAG, "readPages: href ${href.removePrefix("/")}")
                        if (entry.name.endsWith(".xhtml")) {
                            zipFile.getInputStream(entry).use { inputStream ->
                                val document: Document =
                                    Jsoup.parse(inputStream, "UTF-8", "", xmlParser())
                                val text = document.text()
                                pagesMap.put(href, text)
                            }
                        }
                    }
                }
                return@withContext pagesMap
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyMap()
            }
        }
    }

    private fun startNotification(title: String, content: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Books Processing Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            (appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
    }

    companion object {
        const val TAG = "PrepareBookForSearchService"
        const val EPUB_FILE_PATH = "EPUB_FILE_PATH"
        const val CHANNEL_ID = "books processing notification"
        const val CHANNEL_DESCRIPTION = "أشعارات تحضير الكتب للبحث"
    }
}
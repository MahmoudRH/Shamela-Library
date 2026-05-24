package com.shamela.apptheme.presentation.worker

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.shamela.apptheme.R
import com.shamela.apptheme.data.db.DatabaseHelper
import com.shamela.apptheme.data.util.ArabicNormalizer
import com.shamela.apptheme.domain.model.BookPage
import com.shamela.apptheme.presentation.util.notifications.ChannelType
import com.shamela.apptheme.presentation.util.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser.xmlParser
import org.readium.r2.streamer.parser.EpubParser
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime


class BookPreparationWorker(
    private val appContext: Context,
    private val params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val notificationId = id.hashCode()
        val notification = NotificationHelper.startNotification(
            context = appContext,
            type = ChannelType.BookPreparation,
            title = appContext.getString(R.string.book_preparation_notification_title),
            content = appContext.getString(R.string.book_preparation_notification_content)
        )

        setForegroundAsync(createForegroundInfo(notificationId, notification))

        return withContext(Dispatchers.IO) {
            val bookFilePath = params.inputData.getString(EPUB_FILE_PATH)
                ?: return@withContext Result.failure()

            var readPagesTime = System.currentTimeMillis()
            val pages = getPages(bookFilePath)
            readPagesTime = System.currentTimeMillis() - readPagesTime
            Log.e(TAG, "readPagesTime = ${readPagesTime.milliseconds}")
            val bookInsertionTime = measureTime {
                insertBookPages(bookFilePath, pages)
            }
            Log.e(TAG, "bookInsertionTime = $bookInsertionTime")
            Result.success()
        }
    }

    private fun createForegroundInfo(id: Int, notification: Notification): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= 34) {
            ForegroundInfo(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(id, notification)
        }
    }

    private suspend fun insertBookPages(bookFilePath: String, pages: Map<String, String>) {
        val database = DatabaseHelper(appContext)
        val bookFile = File(bookFilePath)
        val bookTitle = bookFile.name.removeSuffix(".epub")
        val category = bookFile.parentFile?.name ?: "unknown"
        val bookID = UUID.nameUUIDFromBytes((bookTitle + category).toByteArray()).toString()

        var pageMappingTime = System.currentTimeMillis()
        val pagesList = pages.map { (href, content) ->
            BookPage(
                href = href,
                content = content,
                bookId = bookID,
                category = category,
                bookTitle = bookTitle
            )
        }
        pageMappingTime = System.currentTimeMillis() - pageMappingTime
        Log.e(
            TAG,
            "pageMappingTime = ${pageMappingTime.milliseconds}, pageListSize = ${pagesList.size}"
        )

        database.insertBookPages(pagesList)

        database.close()
    }

    private suspend fun getPages(bookFilePath: String): Map<String, String> {
        val normalizer = ArabicNormalizer()

        return withContext(Dispatchers.IO) {
            val hrefs = try {
                EpubParser().parse(bookFilePath)
                    ?.publication
                    ?.readingOrder
                    ?.mapNotNull { it.href }
                    ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse EPUB: ${e.message}")
                emptyList()
            }

            val pagesMap = mutableMapOf<String, String>()
            try {
                ZipFile(File(bookFilePath)).use { zipFile ->
                    hrefs.forEach { href ->
                        val entryName = href.removePrefix("/")
                        if (entryName.endsWith(".xhtml")) {
                            val entry = ZipEntry(entryName)
                            zipFile.getInputStream(entry).use { inputStream ->
                                val document = Jsoup.parse(inputStream, "UTF-8", "", xmlParser())
                                val normalizedText = normalizer.normalize(document.text())
                                pagesMap[href] = normalizedText
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading ZIP: ${e.message}")
            }

            pagesMap
        }
    }

    companion object {
        private const val TAG = "BookPreparationWorker"
        const val EPUB_FILE_PATH = "EPUB_FILE_PATH"
    }
}

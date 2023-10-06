package com.shamela.apptheme.presentation.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.shamela.apptheme.data.db.DatabaseHelper
import com.shamela.apptheme.data.util.ArabicNormalizer
import com.shamela.apptheme.domain.model.BookPage
import com.shamela.apptheme.presentation.util.ChannelType
import com.shamela.apptheme.presentation.util.startNotification
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
            context = appContext, title = "تحضير الكتب للبحث",
            type = ChannelType.BookPreparation,
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

                val isSuccess = database.insertBookPage(page)
                Log.e(TAG, "doWork: pageInserted $isSuccess")

            }
            database.close()
            return@withContext Result.success()
        }
    }

    private suspend fun getPages(bookFilePath: String): Map<String, String> {
        val normalizer = ArabicNormalizer()
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
                                val pageTextNormalized = normalizer.normalize( document.text())
                                pagesMap.put(href, pageTextNormalized)
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

    companion object {
        const val TAG = "PrepareBookForSearchService"
        const val EPUB_FILE_PATH = "EPUB_FILE_PATH"
    }
}
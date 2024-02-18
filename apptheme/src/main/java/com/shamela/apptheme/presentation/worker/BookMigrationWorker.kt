package com.shamela.apptheme.presentation.worker

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class BookMigrationWorker(
    private val appContext: Context,
    private val params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @OptIn(ExperimentalTime::class)
    @SuppressLint("Range")
    override suspend fun doWork(): Result {
        Log.e("BookMigrationWorker", "doWork: isCalled", )
        val notification = startNotification(
            context = appContext,
            type = ChannelType.DatabaseMigration,
            title = "تحديث قاعدة البيانات",
            content = "يتم تحديث قاعدة البيانات.."
        )
        val notificationId = this.id.hashCode()
        val foreground = if (Build.VERSION.SDK_INT >= 34) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
        setForegroundAsync(foreground)

        return withContext(Dispatchers.IO) {
            Log.e("BookMigrationWorker", "doWork: isRunning", )
            val totalTime = measureTime {
            val database = DatabaseHelper(appContext).writableDatabase
            val normalizer = ArabicNormalizer()
            val cursor = database.query(BookPage.TABLE_NAME, arrayOf(BookPage.COL_ID, BookPage.COL_CONTENT), null, null, null, null, null)
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(BookPage.COL_ID))
                val content = cursor.getString(cursor.getColumnIndex(BookPage.COL_CONTENT))
                val normalizedContent = normalizer.normalize(content)

                val contentValues = ContentValues()
                contentValues.put(BookPage.COL_CONTENT, normalizedContent)

                database.update(BookPage.TABLE_NAME, contentValues, "${BookPage.COL_ID}=?", arrayOf(id))

            }
            cursor.close()
            }
            Log.e("BookMigrationWorker", "doWork: totalTime= ${totalTime.inWholeSeconds}s", )
            Result.success()
        }
    }
}
package com.shamela.apptheme.presentation.worker


import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.shamela.apptheme.data.db.DatabaseHelper
import com.shamela.apptheme.domain.model.BookPage
import com.shamela.apptheme.presentation.util.notifications.ChannelType
import com.shamela.apptheme.presentation.util.notifications.NotificationHelper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BookMigrationWorkerTest {
    private fun setupNotificationChannels(context: Context) {
        ChannelType.entries.forEach { NotificationHelper.createChannel(context, it) }
    }

    private lateinit var context: Context
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbHelper = DatabaseHelper(context)
        setupNotificationChannels(getApplicationContext())

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
    }

    private fun waitForWorkToFinish(id: UUID, timeoutMs: Long = 5000) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val state = workManager.getWorkInfoById(id).get()?.state
            if (state?.isFinished == true) return
            Thread.sleep(50)
        }
        throw AssertionError("Work did not finish in time")
    }

    @After
    fun tearDown() {
        dbHelper = DatabaseHelper(context)
        dbHelper.use {
            it.writableDatabase.execSQL("DELETE FROM ${BookPage.TABLE_NAME};")
        }
    }

    @Test
    fun bookMigrationWorker_normalizesContentCorrectly() {
        val db = dbHelper.writableDatabase
        db.insert(BookPage.TABLE_NAME, null, ContentValues().apply {
            put(BookPage.COL_ID, "1")
            put(BookPage.COL_CONTENT, "الْكِتَابُ")
        })

        val request = OneTimeWorkRequestBuilder<BookMigrationWorker>().build()
        workManager.enqueue(request)
        waitForWorkToFinish(request.id)

        val content = db.query(BookPage.TABLE_NAME, arrayOf(BookPage.COL_CONTENT), null, null, null, null, null)
            .use {
                it.moveToFirst()
                it.getString(it.getColumnIndexOrThrow(BookPage.COL_CONTENT))
            }

        assertEquals("الكتاب", content)
    }

    @Test
    fun bookMigrationWorker_skipsAlreadyNormalizedContent() {
        val db = dbHelper.writableDatabase
        db.insert(BookPage.TABLE_NAME, null, ContentValues().apply {
            put(BookPage.COL_ID, "2")
            put(BookPage.COL_CONTENT, "الكتاب")
        })

        val request = OneTimeWorkRequestBuilder<BookMigrationWorker>().build()
        workManager.enqueue(request)
        waitForWorkToFinish(request.id)

        val content = db.query(BookPage.TABLE_NAME, arrayOf(BookPage.COL_CONTENT), null, null, null, null, null)
            .use {
                it.moveToFirst()
                it.getString(it.getColumnIndexOrThrow(BookPage.COL_CONTENT))
            }

        assertEquals("الكتاب", content)
    }

    @Test
    fun bookMigrationWorker_normalizesMultiplePages() {
        val db = dbHelper.writableDatabase
        val inputs = listOf("الْكِتَابُ", "وَالْقَلَمِ", "مُعَلِّمٌ")
        val expected = listOf("الكتاب", "والقلم", "معلم")

        inputs.forEachIndexed { i, content ->
            db.insert(BookPage.TABLE_NAME, null, ContentValues().apply {
                put(BookPage.COL_ID, (i + 1).toString())
                put(BookPage.COL_CONTENT, content)
            })
        }

        val request = OneTimeWorkRequestBuilder<BookMigrationWorker>().build()
        workManager.enqueue(request)
        waitForWorkToFinish(request.id)

        val results = mutableListOf<String>()
        val cursor = db.query(BookPage.TABLE_NAME, arrayOf(BookPage.COL_CONTENT), null, null, null, null, null)
        while (cursor.moveToNext()) {
            results.add(cursor.getString(cursor.getColumnIndexOrThrow(BookPage.COL_CONTENT)))
        }
        cursor.close()

        assertEquals(expected, results)
    }

    @Test
    fun bookMigrationWorker_handlesEmptyTableGracefully() {
        val request = OneTimeWorkRequestBuilder<BookMigrationWorker>().build()
        workManager.enqueue(request)
        waitForWorkToFinish(request.id)

        val state = workManager.getWorkInfoById(request.id).get()?.state
        assertThat(state, `is`(WorkInfo.State.SUCCEEDED))
    }

    @Test
    fun bookMigrationWorker_handlesLargeDataset() {
        val db = dbHelper.writableDatabase
        val diacriticText = "مُحَمَّدٌ"

        // Insert 1000 records with diacritics
        db.beginTransaction()
        try {
            repeat(10000) { i ->
                val values = ContentValues().apply {
                    put(BookPage.COL_ID, i.toString())
                    put(BookPage.COL_CONTENT, diacriticText)
                }
                db.insert(BookPage.TABLE_NAME, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        val request = OneTimeWorkRequestBuilder<BookMigrationWorker>().build()
        workManager.enqueue(request)
        waitForWorkToFinish(request.id, timeoutMs = 100_000) // Give more time for large input

        // Validate all rows were normalized
        val cursor = db.query(BookPage.TABLE_NAME, arrayOf(BookPage.COL_CONTENT), null, null, null, null, null)
        var normalizedCount = 0
        while (cursor.moveToNext()) {
            val content = cursor.getString(cursor.getColumnIndexOrThrow(BookPage.COL_CONTENT))
            if (content == "محمد") normalizedCount++
        }
        cursor.close()

        assertEquals(10000, normalizedCount)
    }
}

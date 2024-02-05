package com.shamela.apptheme.data.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.shamela.apptheme.data.util.ArabicNormalizer
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteOpenHelper
import com.shamela.apptheme.domain.model.BookPage
import com.shamela.apptheme.presentation.worker.BookMigrationWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseHelper(val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val db: SQLiteDatabase = this.writableDatabase

    companion object {
        const val DATABASE_NAME = "AppDatabase"
        const val DATABASE_VERSION = 2
    }

    override fun onCreate(database: SQLiteDatabase) {
        Log.e("DatabaseHelper", "onCreate: isCalled")
        database.execSQL(BookPage.CREATE_TABLE)
    }


    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.e("DatabaseHelper", "onUpgrade: isCalled")
        if (oldVersion == 1 && newVersion == 2) {
            // Perform the migration from v1 to v2
            val migrationWorkRequest = OneTimeWorkRequestBuilder<BookMigrationWorker>().build()
            WorkManager.getInstance(context).enqueue(migrationWorkRequest)
            Log.e("DatabaseHelper", "onUpgrade: migration enqueued")
        } else {
            database.execSQL("DROP TABLE IF EXISTS ${BookPage.TABLE_NAME} ;")
            onCreate(database)
            Log.e("DatabaseHelper", "onUpgrade: database dropped")
        }
    }

    suspend fun insertBookPage(page: BookPage): Boolean {
        return withContext(Dispatchers.IO) {
            val contentValues = ContentValues().apply {
                put(BookPage.COL_ID, page.id)
                put(BookPage.COL_BOOK_ID, page.bookId)
                put(BookPage.COL_BOOK_TITLE, page.bookTitle)
                put(BookPage.COL_HREF, page.href)
                put(BookPage.COL_CATEGORY, page.category)
                put(BookPage.COL_CONTENT, page.content)
            }

            return@withContext db.insert(BookPage.TABLE_NAME, null, contentValues) > 0
        }
    }

    @SuppressLint("Range")
    suspend fun searchBook(bookId: String, query: String): List<BookPage> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<BookPage>()
            val queryNormalized = ArabicNormalizer().normalize(query)
            val cursor = db.query(
                BookPage.TABLE_NAME,
                arrayOf("*"),
                "${BookPage.COL_BOOK_ID} = ? AND ${BookPage.COL_CONTENT} MATCH ?",
                arrayOf(bookId, "\"${queryNormalized}\""),
                null,
                null,
                "rank"
            )
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val page = BookPage(
                    href = cursor.getString(cursor.getColumnIndex(BookPage.COL_HREF)),
                    content = cursor.getString(cursor.getColumnIndex(BookPage.COL_CONTENT)),
                    bookId = cursor.getString(cursor.getColumnIndex(BookPage.COL_BOOK_ID)),
                    category = cursor.getString(cursor.getColumnIndex(BookPage.COL_CATEGORY)),
                    bookTitle = cursor.getString(cursor.getColumnIndex(BookPage.COL_BOOK_TITLE))
                )
                results.add(page)
                cursor.moveToNext()
            }
            cursor.close()
            return@withContext results
        }

    }

    @SuppressLint("Range")
    suspend fun searchCategory(category: String, query: String): List<BookPage> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<BookPage>()
            val cursor = db.query(
                BookPage.TABLE_NAME,
                arrayOf("*"),
                "${BookPage.COL_CATEGORY} = ? AND ${BookPage.COL_CONTENT} MATCH ?",
                arrayOf(category, "\"${query}\""),
                null,
                null,
                "rank"
            )
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val page = BookPage(
                    href = cursor.getString(cursor.getColumnIndex(BookPage.COL_HREF)),
                    content = cursor.getString(cursor.getColumnIndex(BookPage.COL_CONTENT)),
                    bookId = cursor.getString(cursor.getColumnIndex(BookPage.COL_BOOK_ID)),
                    category = cursor.getString(cursor.getColumnIndex(BookPage.COL_CATEGORY)),
                    bookTitle = cursor.getString(cursor.getColumnIndex(BookPage.COL_BOOK_TITLE))
                )
                results.add(page)
                cursor.moveToNext()
            }
            cursor.close()
            return@withContext results
        }
    }

    @SuppressLint("Range")
    suspend fun searchLibrary(query: String): List<BookPage> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<BookPage>()
            val cursor = db.query(
                BookPage.TABLE_NAME,
                arrayOf("*"),
                "${BookPage.COL_CONTENT} MATCH ?",
                arrayOf("\"${query}\""),
                null,
                null,
                "rank"
            )
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val page = BookPage(
                    href = cursor.getString(cursor.getColumnIndex(BookPage.COL_HREF)),
                    content = cursor.getString(cursor.getColumnIndex(BookPage.COL_CONTENT)),
                    bookId = cursor.getString(cursor.getColumnIndex(BookPage.COL_BOOK_ID)),
                    category = cursor.getString(cursor.getColumnIndex(BookPage.COL_CATEGORY)),
                    bookTitle = cursor.getString(cursor.getColumnIndex(BookPage.COL_BOOK_TITLE))
                )
                results.add(page)
                cursor.moveToNext()
            }
            cursor.close()
            return@withContext results
        }
    }
}
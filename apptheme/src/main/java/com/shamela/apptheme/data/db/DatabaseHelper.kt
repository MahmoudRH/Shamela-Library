package com.shamela.apptheme.data.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteOpenHelper
import com.shamela.apptheme.domain.model.BookPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val db: SQLiteDatabase = this.writableDatabase

    companion object {
        const val DATABASE_NAME = "AppDatabase"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(BookPage.CREATE_TABLE)
    }

    override fun onUpgrade(database: SQLiteDatabase, p1: Int, p2: Int) {
        database.execSQL("DROP TABLE IF EXISTS ${BookPage.TABLE_NAME} ;")
        onCreate(database)
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
            val cursor = db.rawQuery(
                "SELECT * FROM ${BookPage.TABLE_NAME} WHERE ${BookPage.COL_BOOK_ID} = ? AND ${BookPage.COL_CONTENT} MATCH ? ORDER BY rank;",
                arrayOf(bookId, query)
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

            val cursor = db.rawQuery(
                "SELECT * FROM ${BookPage.TABLE_NAME} WHERE ${BookPage.COL_CATEGORY} = ? AND ${BookPage.COL_CONTENT} MATCH ? ORDER BY rank;",
                arrayOf(category, query)
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
            val cursor = db.rawQuery(
                "SELECT * FROM ${BookPage.TABLE_NAME} WHERE ${BookPage.COL_CONTENT} MATCH ? ORDER BY rank;",
                arrayOf(query)
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

    fun closeConnection(){
        this.close()
    }
}
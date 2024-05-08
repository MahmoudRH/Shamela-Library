package com.shamela.library.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Quote

@Database(entities = [Book::class, Quote::class], version = 3, exportSchema = false)
abstract class BooksDatabase : RoomDatabase() {
    abstract val booksDao: BooksDao
    abstract val quotesDao: QuotesDao
    companion object {
        const val DATABASE_NAME = "BooksDatabase"
    }
}
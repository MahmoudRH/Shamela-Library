package com.shamela.library.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shamela.library.domain.model.Book

@Database(entities = [Book::class], version = 1, exportSchema = false)
abstract class BooksDatabase : RoomDatabase() {
    abstract val booksDao: BooksDao

    companion object {
        const val DATABASE_NAME = "BooksDatabase"
    }
}
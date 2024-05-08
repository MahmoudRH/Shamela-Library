package com.shamela.apptheme.data.db

import com.shamela.apptheme.domain.datasource.BookPagesRepo
import com.shamela.apptheme.domain.model.BookPage

class BookPagesRepoImpl(private val databaseHelper: DatabaseHelper):BookPagesRepo {
    override suspend fun insertPage(page: BookPage): Boolean {
        return databaseHelper.insertBookPage(page)
    }

    override suspend fun searchBook(bookId: String, query: String): List<BookPage> {
        return databaseHelper.searchBook(bookId, query)
    }

    override suspend fun searchCategory(category: String, query: String): List<BookPage> {
        return databaseHelper.searchCategory(category, query)
    }

    override suspend fun searchLibrary(query: String): List<BookPage> {
        return databaseHelper.searchLibrary(query)
    }
}
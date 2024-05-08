package com.shamela.library.domain.usecases.books

import com.shamela.library.data.local.db.BooksDao
import com.shamela.library.domain.model.Book

/** For saving downloaded books into the database*/
class SaveDownloadedBook(private val dao: BooksDao) {
    suspend operator fun invoke(book: Book) {
        dao.insert(book)
    }
}
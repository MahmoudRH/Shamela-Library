package com.shamela.library.domain.usecases.books

import com.shamela.library.data.local.db.BooksDao
import com.shamela.library.domain.model.Book

class GetBookById(private val dao: BooksDao) {
    suspend operator fun invoke(bookId:String): Book? {
         return dao.getBookById(bookId)
    }
}
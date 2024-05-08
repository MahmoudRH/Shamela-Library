package com.shamela.library.domain.usecases.books

import com.shamela.library.data.local.db.BooksDao

class UpdateBook (private val dao: BooksDao) {
    /** @param isFavorite: accepts 0,1 values for false, true */
    suspend operator fun invoke(bookId:String, isFavorite:Int){
        dao.updateBook(bookId,isFavorite.coerceIn(0,1))
    }
}
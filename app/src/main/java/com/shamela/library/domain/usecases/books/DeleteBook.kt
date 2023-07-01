package com.shamela.library.domain.usecases.books

import com.shamela.library.data.local.db.BooksDao

class DeleteBook(private val dao: BooksDao) {
    suspend operator fun invoke(bookId:String){
        dao.delete(bookId)
    }
}
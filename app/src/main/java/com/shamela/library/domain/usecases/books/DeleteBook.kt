package com.shamela.library.domain.usecases.books

import com.shamela.library.data.local.db.BooksDao
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.domain.model.Book

class DeleteBook(private val dao: BooksDao) {
    suspend operator fun invoke(book: Book): Boolean {
        deleteBookFromLocalDB(book.id,dao)
       return deleteBookFromLocalStorage(book)
    }

    private suspend fun deleteBookFromLocalDB(bookId:String, dao: BooksDao){
        dao.delete(bookId)
    }

    private suspend fun deleteBookFromLocalStorage(book: Book): Boolean {
       return FilesBooksRepoImpl.deleteBookFile(book)
    }
}

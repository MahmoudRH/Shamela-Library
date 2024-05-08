package com.shamela.library.domain.usecases.books

import com.shamela.library.data.local.db.BooksDao
import com.shamela.library.domain.model.Book
import kotlinx.coroutines.flow.Flow


class GetFavoriteBooks (private val dao: BooksDao){
    suspend operator fun invoke(): Flow<List<Book>> {
       return dao.getFavoriteBooks()
    }
}
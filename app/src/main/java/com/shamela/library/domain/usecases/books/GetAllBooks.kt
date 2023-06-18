package com.shamela.library.domain.usecases.books

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.repo.BooksRepository
import kotlinx.coroutines.flow.Flow

class GetAllBooks(private val repo: BooksRepository) {
    operator fun invoke(): Flow<Book> {
        return repo.getAllBooks()
    }
}
package com.shamela.library.domain.usecases.books

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.repo.BooksRepository
import kotlinx.coroutines.flow.Flow

class GetBooksByCategory(private val repo: BooksRepository) {
    operator fun invoke(categoryName: String): Flow<Book> {
        return repo.getBooksByCategory(categoryName)
    }
}
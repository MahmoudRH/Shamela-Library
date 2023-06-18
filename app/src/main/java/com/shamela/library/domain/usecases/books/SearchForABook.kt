package com.shamela.library.domain.usecases.books

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.repo.BooksRepository
import kotlinx.coroutines.flow.Flow

class SearchForABook(private val repo: BooksRepository) {
     operator fun invoke(query: String): Flow<Book> {
        return repo.searchBooksByName(query)
    }
}
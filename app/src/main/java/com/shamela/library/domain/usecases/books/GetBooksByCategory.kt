package com.shamela.library.domain.usecases.books

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.repo.BooksRepository
import javax.inject.Inject

class GetBooksByCategory @Inject constructor(private val repo: BooksRepository) {
    suspend operator fun invoke(categoryName: String): List<Book> {
        return repo.getBooksByCategory(categoryName)
    }
}
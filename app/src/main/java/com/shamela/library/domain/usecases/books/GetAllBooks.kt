package com.shamela.library.domain.usecases.books

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.repo.BooksRepository
import javax.inject.Inject

class GetAllBooks(private val repo: BooksRepository) {
    suspend operator fun invoke(): List<Book> {
        return repo.getAllBooks()
    }
}
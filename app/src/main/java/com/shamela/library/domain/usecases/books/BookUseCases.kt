package com.shamela.library.domain.usecases.books

import com.shamela.library.domain.repo.BooksRepository

data class BooksUseCases(
    val repository: BooksRepository,
    val getAllCategories: GetAllCategories = GetAllCategories(repository),
    val getAllBooks: GetAllBooks = GetAllBooks(repository),
    val getBooksByCategory: GetBooksByCategory = GetBooksByCategory(repository),
    val searchForABook: SearchForABook = SearchForABook(repository),
    val getDownloadUri: GetDownloadUri = GetDownloadUri(repository),
)
package com.shamela.library.domain.usecases.books

data class BooksUseCases(
    val getAllCategories: GetAllCategories,
    val getAllBooks: GetAllBooks,
    val getBooksByCategory: GetBooksByCategory,
    val searchForABook: SearchForABook
)
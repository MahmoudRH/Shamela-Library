package com.shamela.library.domain.usecases.books

import com.shamela.library.data.local.db.BooksDao
import com.shamela.library.domain.repo.BooksRepository

data class BooksUseCases(
    val repository: BooksRepository,
    val booksDao: BooksDao,
    val getAllCategories: GetAllCategories = GetAllCategories(repository),
    val getAllBooks: GetAllBooks = GetAllBooks(repository),
    val getBooksByCategory: GetBooksByCategory = GetBooksByCategory(repository),
    val searchForABook: SearchForABook = SearchForABook(repository),
    val getDownloadUri: GetDownloadUri = GetDownloadUri(repository),
    val getFavoriteBooks: GetFavoriteBooks = GetFavoriteBooks(booksDao),
    val getDownloadedBooks: GetDownloadedBooks = GetDownloadedBooks(booksDao),
    val updateBook: UpdateBook = UpdateBook(booksDao),
    val saveDownloadedBook: SaveDownloadedBook = SaveDownloadedBook(booksDao),
    val deleteBook: DeleteBook = DeleteBook(booksDao),
)
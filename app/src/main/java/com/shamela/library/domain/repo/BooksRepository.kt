package com.shamela.library.domain.repo

import android.net.Uri
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun getCategories(): Flow<Category>
    fun getBooksByCategory(categoryName: String): Flow<Book>
    fun searchBooksByName(categoryName: String, query: String): Flow<Book>
    fun getAllBooks(): Flow<Book>
    suspend fun getDownloadLink(categoryName: String, bookName: String): Uri?
}
package com.shamela.library.domain.repo

import android.net.Uri
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category

interface BooksRepository {
    suspend fun getCategories(): List<Category>
    suspend fun getBooksByCategory(categoryName: String): List<Book>
    suspend fun searchBooksByName(query: String): List<Book>
    suspend fun getAllBooks():List<Book>
    suspend fun getDownloadLink(categoryName: String, bookName:String): Uri?
}
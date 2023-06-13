package com.shamela.library.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.repo.BooksRepository
import kotlinx.coroutines.tasks.await

class BooksRepoImpl :BooksRepository {

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    override suspend fun getCategories(): List<Category> {
        return emptyList()
    }


    override suspend fun getBooksByCategory(categoryName: String): List<Book> {
        val books = mutableListOf<Book>()

        try {
            val categoryFolderRef = storage.reference.child("shamela_epub/$categoryName")
            val files = categoryFolderRef.listAll().await()

            for (fileRef in files.items) {
                val bookName = fileRef.name
                val authorName = "Unknown" // Set author name based on your logic
                val pageCount = 0 // Set page count based on your logic

                val book = Book("", bookName, authorName, pageCount,"")
                books.add(book)
            }
        } catch (e: Exception) {
            // Handle the exception
        }

        return books
    }

    override suspend fun searchBooksByName(query: String): List<Book> {
        return emptyList()
    }

    override suspend fun getAllBooks(): List<Book> {
        return emptyList()
    }

    override suspend fun getDownloadLink(categoryName: String, bookName: String): Uri {
        TODO("Not yet implemented")
    }
}
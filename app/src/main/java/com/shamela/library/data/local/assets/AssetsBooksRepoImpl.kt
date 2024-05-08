package com.shamela.library.data.local.assets

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.shamela.library.data.local.assets.dto.AssetsBook
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.repo.BooksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID
import javax.inject.Qualifier

@Qualifier
annotation class AssetsRepoImpl
class AssetsBooksRepoImpl(private val context: Context) : BooksRepository {
    private val TAG = "AssetsBooksRepoImpl"
    private val gson = Gson()
    private val categoryBookCounts: MutableMap<String, Int> = mutableMapOf()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    suspend fun _getCategories(): List<Category> {
        return withContext(Dispatchers.IO) {
            try {
                val categoryNames = context.assets.list("categories")
                categoryNames?.mapIndexed { index, categoryName ->
                    val bookCount = categoryBookCounts.getOrPut(categoryName) {
                        _getBooksByCategory(categoryName).size
                    }
                    Category(index.toString(), categoryName.removeSuffix(".json"), bookCount)
                } ?: emptyList()
            } catch (e: IOException) {
                Log.e(TAG, "Error: getCategories(). ${e.message}")
                emptyList()
            }
        }
    }

    private suspend fun _getBooksByCategory(categoryName: String): List<Book> {
        return withContext(Dispatchers.IO) {
            try {
                val categoryNameNoSuffix = categoryName.removeSuffix(".json")
                val fileName = "categories/$categoryNameNoSuffix.json"
                val inputStream = context.assets.open(fileName)
                val books =
                    gson.fromJson(InputStreamReader(inputStream), Array<AssetsBook>::class.java)
                inputStream.close()
                books.toList()
                    .map { book ->
                        val uuidName = book.title + categoryNameNoSuffix
                        val bookID = UUID.nameUUIDFromBytes(uuidName.toByteArray()).toString()
                        Book(bookID, book.title, book.author, book.pageCount, categoryNameNoSuffix)
                    }
            } catch (e: IOException) {
                Log.e(TAG, "Error: getBooksByCategory($categoryName). ${e.message}")
                emptyList()
            }
        }
    }

//    private suspend fun _getsAllBooks(): List<Book> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val categoryNames = context.assets.list("categories") ?: emptyArray()
//                val allBooks = mutableListOf<Book>()
//                for (categoryName in categoryNames) {
//                    val books = _getBooksByCategory(categoryName)
//                    allBooks.addAll(books)
//                }
//                allBooks
//            } catch (e: IOException) {
//                Log.e(TAG, "Error: getAllBooks. ${e.message}")
//                emptyList()
//            }
//        }
//    }

    override fun getCategories(): Flow<Category> = flow {
        emitAll(_getCategories().asFlow())
    }

    override fun getBooksByCategory(categoryName: String): Flow<Book> = flow {
        emitAll(_getBooksByCategory(categoryName).asFlow())
    }

    override fun searchBooksByName(categoryName: String, query: String): Flow<Book> {
        return if (categoryName == "all") getAllBooks()
            .filter { it.title.contains(query) }
        else {
            getBooksByCategory(categoryName).filter { it.title.contains(query) }
        }
    }

    override fun getAllBooks(): Flow<Book> = flow {
        try {
            val categoryNames = context.assets.list("categories") ?: emptyArray()
            categoryNames.forEach { category ->
                val books = _getBooksByCategory(category).asFlow()
                emitAll(books)
            }

        } catch (e: IOException) {
            Log.e(TAG, "Error: getAllBooks. ${e.message}")
        }
    }

    override suspend fun getDownloadLink(categoryName: String, bookName: String): Uri? {
        Log.d(TAG, "getDownloadLink: (categoryName, bookName ) = ($categoryName, $bookName)", )
        try {
            val bookRef = storage.reference.child("shamela_epub/$categoryName/$bookName.epub")
            return bookRef.downloadUrl.await()
        } catch (e: Exception) {
            Log.e(TAG, "ERROR: getDownloadLink: (categoryName, bookName ) = ($categoryName, $bookName)", )
            e.printStackTrace()
        }
        return null
    }


}
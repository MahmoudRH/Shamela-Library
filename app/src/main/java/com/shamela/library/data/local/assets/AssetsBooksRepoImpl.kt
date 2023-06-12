package com.shamela.library.data.local.assets

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.repo.BooksRepository
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader

class AssetsBooksRepoImpl(private val context: Context) : BooksRepository {
    private  val TAG = "AssetsBooksRepoImpl"
    private val gson = Gson()
    private val categoryBookCounts: MutableMap<String, Int> = mutableMapOf()

    override suspend fun getCategories(): List<Category> {
        return withContext(Dispatchers.IO) {
            try {
                val categoryNames = context.assets.list("categories")
                categoryNames?.mapIndexed { index, categoryName ->
                    val bookCount = categoryBookCounts.getOrPut(categoryName) {
                        getBooksByCategory(categoryName).size
                    }
                    Category(index.toString(), categoryName.removeSuffix(".json"), bookCount)
                } ?: emptyList()
            } catch (e: IOException) {
                Log.e(TAG, "Error: getCategories(). ${e.message}", )
                emptyList()
            }
        }
    }

    override suspend fun getBooksByCategory(categoryName: String): List<Book> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "categories/$categoryName"
                val inputStream = context.assets.open(fileName)
                val books = gson.fromJson(InputStreamReader(inputStream), Array<Book>::class.java)
                inputStream.close()
                books.toList()
            } catch (e: IOException) {
                Log.e(TAG, "Error: getBooksByCategory($categoryName). ${e.message}", )
                emptyList()
            }
        }
    }

    override suspend fun searchBooksByName(query: String): List<Book> {
        return getAllBooks().filter { it.title.contains(query) }
    }

    override suspend fun getAllBooks(): List<Book> {
        return withContext(Dispatchers.IO) {
            try {
                val categoryNames = context.assets.list("categories") ?: emptyArray()
                val allBooks = mutableListOf<Book>()

                for (categoryName in categoryNames) {
                    val books = getBooksByCategory(categoryName)
                    allBooks.addAll(books)
                }

                allBooks.distinctBy { it.id }
            } catch (e: IOException) {
                Log.e(TAG, "Error: getAllBooks. ${e.message}", )
                emptyList()
            }
        }
    }
}
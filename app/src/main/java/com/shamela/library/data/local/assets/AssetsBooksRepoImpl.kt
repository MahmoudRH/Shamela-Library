package com.shamela.library.data.local.assets

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson
import com.shamela.library.BuildConfig
import com.shamela.library.data.local.assets.dto.AssetsBook
import com.shamela.library.data.local.assets.dto.AssetsBookDetail
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.BookDetails
import com.shamela.library.domain.model.BookInfoItem
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.repo.BooksRepository
import com.shamela.library.domain.search.BookSearchMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader
import java.text.Normalizer
import java.util.Collections
import java.util.UUID
import javax.inject.Qualifier

@Qualifier
annotation class AssetsRepoImpl
class AssetsBooksRepoImpl(private val context: Context) : BooksRepository {
    private val TAG = "AssetsBooksRepoImpl"
    private val gson = Gson()
    private val categoryBookCounts: MutableMap<String, Int> = mutableMapOf()

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

    /**
     * Loads the "about the book" metadata for a single book from assets/book-details/<category>.json.
     * Resolved by (categoryName, title) so it works for browsing, search, favorites, and downloaded
     * books alike. Returns null when the category has no detail file (e.g. محاضرات مفرغة) or the book
     * isn't found.
     */
    suspend fun getBookDetails(categoryName: String, bookTitle: String): BookDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val categoryNameNoSuffix = categoryName.removeSuffix(".json")
                val fileName = "book-details/$categoryNameNoSuffix.json"
                val details = context.assets.open(fileName).use { inputStream ->
                    gson.fromJson(
                        InputStreamReader(inputStream),
                        Array<AssetsBookDetail>::class.java
                    )
                }
                // Normalize both sides: a downloaded book's title comes from the device filename,
                // which may differ in Unicode normalization (NFC/NFD) from the JSON title.
                val target = Normalizer.normalize(bookTitle, Normalizer.Form.NFC)
                details.firstOrNull {
                    Normalizer.normalize(it.title, Normalizer.Form.NFC) == target
                }?.let { detail ->
                    BookDetails(
                        authorDeathYear = detail.authorDeathYear,
                        about = detail.about.orEmpty().map { BookInfoItem(it.label, it.value) },
                        description = detail.description,
                        descriptionSource = detail.descriptionSource,
                        descriptionUrl = detail.descriptionUrl,
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error: getBookDetails($categoryName, $bookTitle). ${e.message}")
                null
            }
        }
    }

    override fun getCategories(): Flow<Category> = flow {
        emitAll(_getCategories().asFlow())
    }

    override fun getBooksByCategory(categoryName: String): Flow<Book> = flow {
        emitAll(_getBooksByCategory(categoryName).asFlow())
    }

    override fun searchBooksByName(categoryName: String, query: String): Flow<Book> {
        return if (categoryName == "all") searchAllBooks(query)
        else flow {
            val matches = _getBooksByCategory(categoryName)
                .filter { BookSearchMatcher.matches(it, query) }
                .sortedByDescending { BookSearchMatcher.relevanceScore(it, query) }
            emitAll(matches.asFlow())
        }
    }

    private fun searchAllBooks(query: String) = channelFlow<Book> {
        val categoryNames = context.assets.list("categories") ?: emptyArray()
        val results = Collections.synchronizedList(mutableListOf<Book>())

        categoryNames.map { category ->
            launch(Dispatchers.IO) {
                _getBooksByCategory(category)
                    .filter { BookSearchMatcher.matches(it, query) }
                    .forEach { results.add(it) }
            }
        }.joinAll()

        results.sortedByDescending { BookSearchMatcher.relevanceScore(it, query) }
               .forEach { send(it) }
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

    override suspend fun getDownloadLink(
        categoryName: String,
        bookName: String
    ): Uri? {

        return try {

            val encodedPath = Uri.encode("$categoryName/$bookName.epub", "/")

            "${BuildConfig.BASE_URL}/$encodedPath".toUri()

        } catch (e: Exception) {
            Log.e(
                TAG,
                "ERROR: getDownloadLink: (categoryName, bookName) = ($categoryName, $bookName)"
            )
            e.printStackTrace()
            null
        }
    }

}
package com.shamela.library.data.local.files

import android.net.Uri
import android.os.Environment
import android.util.Log
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.repo.BooksRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.streamer.parser.epub.EpubParser
import java.io.File
import java.io.FileFilter
import java.io.IOException
import javax.inject.Qualifier


@Qualifier
annotation class FilesRepoImpl

object FilesBooksRepoImpl : BooksRepository {
    private const val TAG = "FilesBooksRepoImpl"
    private const val BASE_DOWNLOAD_DIRECTORY = "ShamelaDownloads"

    private val downloadsFolder =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private val shamelaBooks = File(downloadsFolder, BASE_DOWNLOAD_DIRECTORY)
    private val downloadedBooks: MutableMap<String, List<Book>> = mutableMapOf()
    private val initializationDeferred: CompletableDeferred<Unit> = CompletableDeferred()
    init {
        CoroutineScope(Dispatchers.IO).launch {
            Log.e(TAG, "Initializing Files Repo: ", )
            setup()
            initializationDeferred.complete(Unit)
            Log.e(TAG, "Files Repo Initialized", )
            Log.e(TAG, "downloadedBooks: $downloadedBooks", )
        }
    }

    override suspend fun getCategories(): List<Category> {
        initializationDeferred.await()
        Log.e(TAG, "Loading ALl Categories", )
        return downloadedBooks.keys.mapIndexed { index, categoryName ->
            Category(
                id = "$index",
                name = categoryName,
                downloadedBooks[categoryName]!!.size
            )
        }
    }

    override suspend fun getBooksByCategory(categoryName: String): List<Book> {
        initializationDeferred.await()
        return downloadedBooks[categoryName] ?: emptyList()
    }

    override suspend fun searchBooksByName(query: String): List<Book> {
        initializationDeferred.await()
        return downloadedBooks.values.flatten().filter { it.title.contains(query) }
    }

    override suspend fun getAllBooks(): List<Book> {
        initializationDeferred.await()
        Log.e(TAG, "Loading ALl Books", )
        return downloadedBooks.values.flatten()
    }

    override suspend fun getDownloadLink(categoryName: String, bookName: String): Uri? {
        initializationDeferred.await()
        return null
    }

    private suspend fun setup() {
        withContext(Dispatchers.IO) {
            try {
                if (shamelaBooks.exists() && shamelaBooks.isDirectory) {
                    val categories = shamelaBooks.listFiles(FileFilter { it.isDirectory })
                    val parsedBooks = categories?.flatMap { folder ->
                        val categoryName = folder.name
                        folder.listFiles()?.toList()?.map { file ->
                            async { parseBook(file, categoryName) }
                        } ?: emptyList()
                    }
                    val books = parsedBooks?.awaitAll()?.filterNotNull()
                    books?.groupBy { it.categoryName }?.let { downloadedBooks.putAll(it) }
                } else {
                    Log.w(TAG, "setup: shamelaBooks doesn't exist")
                }
            } catch (e: IOException) {
                Log.e(TAG, "setup: IOException [${e.message}]")
            }
        }
    }

    private suspend fun parseBook(file: File, categoryName: String): Book? {
        return withContext(Dispatchers.IO) {
            EpubParser().parse(file.path)?.let { pubBox ->
                pubBox.publication.run {
                    Book(
                        id = metadata.identifier ?: "",
                        title = metadata.title,
                        author = metadata.authors.first().name,
                        pageCount = readingOrder.size,
                        categoryName = categoryName
                    )
                }
            }
        }
    }
}
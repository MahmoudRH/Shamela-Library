package com.shamela.library.data.local.files

import android.net.Uri
import android.os.Environment
import android.util.Log
import com.folioreader.FolioReader
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.repo.BooksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.util.UUID
import javax.inject.Qualifier


@Qualifier
annotation class FilesRepoImpl

object FilesBooksRepoImpl : BooksRepository {
    private const val TAG = "FilesBooksRepoImpl"
    private const val BASE_DOWNLOAD_DIRECTORY = "ShamelaDownloads"

    private val downloadsFolder =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private val shamelaBooks = File(downloadsFolder, BASE_DOWNLOAD_DIRECTORY)

    fun openEpub(book: Book) {
        val bookPath = File(shamelaBooks, "${book.categoryName}/${book.title}.epub").path
        FolioReader.get().openBook(bookPath)
    }

    override fun getCategories(): Flow<Category> = channelFlow {
        Log.e(TAG, "Loading ALl Categories")
        try {
            if (shamelaBooks.exists() && shamelaBooks.isDirectory) {
                val categories = shamelaBooks.listFiles(FileFilter { it.isDirectory })
                categories?.forEachIndexed { index, category ->
                    send(Category("$index", category.name, category.listFiles()?.size ?: 0))
                }
            } else {
                Log.w(TAG, "getCategories: shamelaBooks doesn't exist")
            }
        } catch (e: IOException) {
            Log.e(TAG, "getCategories: IOException [${e.message}]")
        }
    }


    override fun getBooksByCategory(categoryName: String): Flow<Book> = channelFlow {
        openShamelaFolder { categoriesFolders ->
            categoriesFolders?.let {
                categoriesFolders.find { it.name == categoryName }?.let { categoryFolder ->
                    val bookFiles = categoryFolder.listFiles()
                    bookFiles?.forEach { bookFile ->
                        val bookTitle = bookFile.name.removeSuffix(".epub")
                        val initialBook = Book(
                            id = UUID.nameUUIDFromBytes(bookTitle.toByteArray()).toString(),
                            title = bookTitle,
                            author = "-",
                            pageCount = 0,
                            categoryName = categoryName
                        )
                        send(initialBook)
                    }
                    bookFiles?.forEach { bookFile ->
                        parseBook(bookFile, categoryName)?.let { book ->
                            send(book)
                        }
                    }
                }
            }
        }
    }

    override fun searchBooksByName(categoryName: String, query: String): Flow<Book> = channelFlow {
        Log.e(TAG, "search Books By Name")
        openShamelaFolder { categories ->
            if (categoryName == "all"){
                categories?.forEach {folder->
                    val bookFile = folder.listFiles()?.find { it.name.contains(query) }
                    bookFile?.let {
                        parseBook(it, categoryName)?.let { book -> send(book) }
                    }
                }
            }else{
                categories?.find { it.name.contains(categoryName) }?.let{folder->
                    val bookFile = folder.listFiles()?.find { it.name.contains(query) }
                    bookFile?.let {
                        parseBook(it, categoryName)?.let { book -> send(book) }
                    }
                }
            }
        }
    }

    override fun getAllBooks(): Flow<Book> = channelFlow {
        Log.e(TAG, "Loading ALl Books")
        openShamelaFolder { categories ->
            categories?.forEach { folder ->
                folder.listFiles()?.forEach { bookFile ->
                    val bookTitle = bookFile.name.removeSuffix(".epub")
                    val initialBook = Book(
                        id = UUID.nameUUIDFromBytes(bookTitle.toByteArray()).toString(),
                        title = bookTitle,
                        author = "-",
                        pageCount = 0,
                        categoryName = folder.name
                    )
                    send(initialBook)
                }
            }
            categories?.forEach { folder ->
                folder.listFiles()?.forEach { bookFile ->
                    parseBook(bookFile, folder.name)?.let { book ->
                        send(book)
                    }
                }
            }
        }
    }


    private suspend fun openShamelaFolder(onOpened: suspend (categories: List<File>?) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                if (shamelaBooks.exists() && shamelaBooks.isDirectory) {
                    val categories = shamelaBooks.listFiles(FileFilter { it.isDirectory })?.toList()
                    onOpened(categories)
                } else {
                    Log.w(TAG, "getCategories: shamelaBooks doesn't exist")
                }
            } catch (e: IOException) {
                Log.e(TAG, "getCategories: IOException [${e.message}]")
            }
        }
    }

    override suspend fun getDownloadLink(categoryName: String, bookName: String): Uri? {
        return null
    }

    private suspend fun parseBook(file: File, categoryName: String): Book? {
        Log.e("Mah ", "parseBook: parsing Book: ${file.name} at $categoryName")

        return withContext(Dispatchers.IO) {
            FolioReader.get().parseEpub(file)?.let { (authorName, pageCount) ->
                val bookTitle = file.name.removeSuffix(".epub")
                Book(
                    id = UUID.nameUUIDFromBytes(bookTitle.toByteArray()).toString(),
                    title = bookTitle,
                    author = authorName,
                    pageCount = pageCount,
                    categoryName = categoryName
                )
            }
        }
    }

}
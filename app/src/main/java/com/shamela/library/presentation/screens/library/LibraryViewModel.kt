package com.shamela.library.presentation.screens.library


import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.utils.FakeRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.readium.r2.streamer.parser.epub.EpubParser
import java.io.File
import java.io.FileFilter
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @FilesRepoImpl private val booksUseCases: BooksUseCases,
) : ViewModel() {
    private val _libraryState = MutableStateFlow<LibraryState>(LibraryState())
    val libraryState = _libraryState.asStateFlow()

    init {
        onEvent(LibraryEvent.LoadUserBooksAndSections)
    }

    fun onEvent(event: LibraryEvent) {
        when (event) {
            is LibraryEvent.OnChangeViewType -> {
                _libraryState.update { it.copy(viewType = event.newViewType) }
            }

            LibraryEvent.LoadUserBooksAndSections -> {
                viewModelScope.launch {
                    _libraryState.update { it.copy(isLoading = true) }
                    val userBooks = booksUseCases.getAllBooks()
                    val userSections = booksUseCases.getAllCategories()
                    _libraryState.update {
                        it.copy(
                            books = userBooks,
                            sections = userSections,
                            isLoading = false
                        )
                    }

                }
            }
        }
    }

/*    fun temp() {
        try {
            val downloadedBooks: MutableMap<Category, List<File>> = mutableMapOf()

            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val shamelaBooks = File(downloadsFolder, "ShamelaDownloads")
            if (shamelaBooks.exists() && shamelaBooks.isDirectory) {
                val categories = shamelaBooks.listFiles(FileFilter { it.isDirectory })
                categories?.forEachIndexed { index, category ->
                    val categoryFiles = category.listFiles()
                    val categoryObject = Category("$index", category.name, categoryFiles?.size ?: 0)
                    categoryFiles?.toList()?.let {
                        downloadedBooks[categoryObject] = it
                    }
                }
                downloadedBooks.forEach { (category, files) ->
                    Log.e("Mah ", "category: ${category.name} booksCount: ${files.size} ")

                    files.forEach { file ->
                        EpubParser().parse(file.path)?.let {
                            val publication = it.publication
                            val title = publication.metadata.title
                            val author = publication.metadata.authors.first().name
                            val identifier = publication.metadata.identifier ?: ""
                            val pageCount =
                                publication.readingOrder.size // Number of pages in the EPUB
                            val book = Book(
                                id = identifier,
                                title = title,
                                author = author,
                                pageCount = pageCount,
                                categoryName = category.name
                            )
                            Log.e("Mah ", "$book ")
                        }
                    }
                }
            } else {
                // Handle the case when the "books" directory doesn't exist or is not a directory
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }*/

}
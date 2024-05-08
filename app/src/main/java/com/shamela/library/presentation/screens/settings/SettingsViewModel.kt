package com.shamela.library.presentation.screens.settings


import android.app.Application
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.shamela.apptheme.presentation.worker.BookPreparationWorker
import com.shamela.library.ShamelaApp
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    @FilesRepoImpl private val booksUseCases: BooksUseCases,
    private val app: Application,
) : ViewModel() {
    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState())
    val settingsState = _settingsState.asStateFlow()


    private suspend fun copyFileToAppFolder(uri: Uri, bookTitle: String): File? {
        return withContext(Dispatchers.IO) {
            ShamelaApp.externalBooksDirectory.run {
                if (!this.exists()) this.mkdirs()
            }
            val bookFileName = "${bookTitle.removeSuffix(".epub")}.epub"
            val destinationFile = File(ShamelaApp.externalBooksDirectory, bookFileName)
            if (destinationFile.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        app.applicationContext,
                        "الكتاب موجود بالفعل",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@withContext null // File already exists, return null
            }
            try {
                app.applicationContext.contentResolver.openInputStream(uri)
                    ?.use { inputStream ->
                        val byteArray = inputStream.readBytes()
                        val size = byteArray.size
                        Log.e("SettingsViewModel", "onEvent: size $size")
                        val outputStream = FileOutputStream(destinationFile)
                        outputStream.write(byteArray)
                        outputStream.close()
                    }
                destinationFile
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "onEvent: Error: ${e.message}")
                Toast.makeText(
                    app.applicationContext,
                    "تعذر إضافة الكتاب للمكتبة",
                    Toast.LENGTH_SHORT
                ).show()
                null
            }
        }

    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnChangeViewType -> {
                _settingsState.update { it.copy(selectedViewType = event.newViewType) }
            }

            is SettingsEvent.AddExternalBookToLibrary -> {
                viewModelScope.launch {
                    _settingsState.update { it.copy(isLoading = true) }
                    withContext(Dispatchers.IO) {
                        copyFileToAppFolder(event.bookUri, event.bookTitle)?.let { bookFile ->
                            FilesBooksRepoImpl.parseBook(
                                bookFile,
                                ShamelaApp.EXTERNAL_BOOKS_CATEGORY
                            )?.let { book ->
                                booksUseCases.saveDownloadedBook(book)
                                val bookFilePath = BooksDownloadManager.getBookPath(book)
                                val workManager = WorkManager.getInstance(app.applicationContext)
                                val request = OneTimeWorkRequestBuilder<BookPreparationWorker>()
                                    .setInputData(workDataOf(BookPreparationWorker.EPUB_FILE_PATH to bookFilePath))
                                    .build()
                                workManager.enqueue(request)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        app.applicationContext,
                                        "تمت اضافة الكتاب بنجاح",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } ?: {
                                Toast.makeText(app.applicationContext, "الكتاب غير متوافق مع المكتبة", Toast.LENGTH_SHORT).show()
                            }
                        }
                        _settingsState.update {
                            it.copy(
                                isLoading = false,
                                fileUri = null,
                                fileName = "اختر كتابا",
                            )
                        }
                    }

                }
            }

            is SettingsEvent.NewFileSelected -> {
                _settingsState.update {
                    it.copy(
                        fileUri = event.fileUri,
                        fileName = getFileNameFromUri(app.applicationContext, event.fileUri),
//                        addStatus = null
                    )
                }
            }
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var fileName = ""
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameColumnIndex: Int = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameColumnIndex != -1) {
                    fileName = it.getString(displayNameColumnIndex)
                }
            }
        }

        cursor?.close()
        return fileName
    }
}
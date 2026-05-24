package com.shamela.library.presentation.screens.settings


import android.app.Application
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.shamela.apptheme.domain.usecases.userPreferences.UserPreferencesUseCases
import com.shamela.apptheme.presentation.settings.PreferenceSettingsEvent
import com.shamela.apptheme.presentation.settings.PreferenceSettingsState
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import com.shamela.apptheme.presentation.worker.BookPreparationWorker
import com.shamela.library.R
import com.shamela.library.ShamelaApp
import com.shamela.library.data.local.files.FilesBooksRepoImpl
import com.shamela.library.data.local.files.FilesRepoImpl
import com.shamela.library.domain.usecases.books.BooksUseCases
import com.shamela.library.presentation.utils.BooksDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    @FilesRepoImpl private val booksUseCases: BooksUseCases,
    private val userPreferencesUseCases: UserPreferencesUseCases,
    private val app: Application,
) : ViewModel() {
    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    private val _toastsChannel = Channel<Int>()
    val toastsChannel = _toastsChannel.receiveAsFlow()

    private val _preferenceSettings = MutableStateFlow<PreferenceSettingsState>(PreferenceSettingsState())
    val preferenceSettings = _preferenceSettings.asStateFlow()

    init {
        initializeSettingsOptions()
        initializeSelection()
    }

    private fun initializeSelection() {
        userPreferencesUseCases.readUserPreferences().let { userPrefs ->
            _preferenceSettings.update { it.copy(userPrefs = userPrefs) }
            val selectedThemePosition =
                preferenceSettings.value.availableFontSizes.indexOf(userPrefs.fontSize)
            _preferenceSettings.update { it.copy(sliderPosition = selectedThemePosition.toFloat()) }
        }
    }

    private fun initializeSettingsOptions() {
        userPreferencesUseCases.getAvailableFontFamilies().let { fonts ->
            _preferenceSettings.update { it.copy(availableFontFamilies = fonts) }
        }
        userPreferencesUseCases.getAvailableFontSizes().let { sizes ->
            _preferenceSettings.update {
                it.copy(availableFontSizes = sizes.map { v -> v.toInt() }.sorted())
            }
        }
        userPreferencesUseCases.getAvailableThemes().let { themes ->
            _preferenceSettings.update { it.copy(availableThemes = themes) }
        }
        userPreferencesUseCases.getAvailableColorSchemes().let { colors ->
            _preferenceSettings.update { it.copy(availableColorSchemes = colors) }
        }
    }


    private suspend fun copyFileToAppFolder(uri: Uri, bookTitle: String): File? {
        return withContext(Dispatchers.IO) {
            ShamelaApp.externalBooksDirectory.run {
                if (!this.exists()) this.mkdirs()
            }
            val bookFileName = "${bookTitle.removeSuffix(".epub")}.epub"
            val destinationFile = File(ShamelaApp.externalBooksDirectory, bookFileName)
            if (destinationFile.exists()) {
                _toastsChannel.send(R.string.the_book_already_exists)
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
                _toastsChannel.send(R.string.could_not_add_book_to_library)
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
                                _toastsChannel.send(R.string.book_added_successfully)
                            } ?: run {
                                _toastsChannel.send(R.string.book_is_not_compatible)
                            }
                        }
                        _settingsState.update {
                            it.copy(
                                isLoading = false,
                                fileUri = null,
                                fileName = null,
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

    fun onPrefsEvent(event: PreferenceSettingsEvent) {
        when (event) {
            is PreferenceSettingsEvent.OnChangeAppFont -> {
                _preferenceSettings.update { it.copy(userPrefs = event.newPrefs) }
                userPreferencesUseCases.updateUserPreferences(event.newPrefs)
                AppFonts.changeFontFamily(AppFonts.fontFamilyOf(event.newPrefs.fontFamily))
            }

            is PreferenceSettingsEvent.OnChangeAppTheme -> {
                _preferenceSettings.update { it.copy(userPrefs = event.userPrefs) }
                userPreferencesUseCases.updateUserPreferences(event.userPrefs)
                AppTheme.changeColorScheme(event.colorScheme, event.userPrefs.theme)
            }

            is PreferenceSettingsEvent.OnChangeAppFontSize -> {
                _preferenceSettings.update { it.copy(userPrefs = event.newPrefs) }
                userPreferencesUseCases.updateUserPreferences(event.newPrefs)
                AppFonts.changeFontSize(event.newPrefs.fontSize)
            }

            is PreferenceSettingsEvent.OnChangeSliderPosition -> {
                _preferenceSettings.update { it.copy(sliderPosition = event.newPosition) }
            }
        }
    }

}
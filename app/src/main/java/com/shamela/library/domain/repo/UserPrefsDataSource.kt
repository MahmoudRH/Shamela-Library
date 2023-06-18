package com.shamela.library.domain.repo

import android.net.Uri
import com.shamela.library.domain.model.UserPrefs

interface UserPrefsDataSource {
    fun getUserPrefs(): UserPrefs
    fun updateUserPrefs(prefs: UserPrefs)

    fun saveAvailableFontFamilies(fonts: Set<String>)
    fun getAvailableFontFamilies(): List<String>

    fun saveAvailableFontSizes(fonts: Set<String>)
    fun getAvailableFontSizes(): List<String>

    fun saveAvailableThemes(themes: Set<String>)
    fun getAvailableThemes(): List<String>

    fun saveAvailableColorSchemes(colorSchemes: Set<String>)
    fun getAvailableColorSchemes(): List<String>

    fun saveLibraryFolderUri(uri: Uri)
    fun getLibraryFolderUri(): Uri?
}
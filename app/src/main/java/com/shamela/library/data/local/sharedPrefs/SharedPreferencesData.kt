package com.shamela.library.data.local.sharedPrefs

import android.content.Context
import android.net.Uri
import com.shamela.library.domain.model.UserPrefs
import com.shamela.library.domain.repo.UserPrefsDataSource

class SharedPreferencesData(private val context: Context) : UserPrefsDataSource {
    override fun getUserPrefs(): UserPrefs {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        var userPrefs = UserPrefs()
        sharedPrefs.getString(preferredTheme, null)?.let {
            userPrefs = userPrefs.copy(theme = it)
        }
        sharedPrefs.getString(preferredFontFamily, null)?.let {
            userPrefs = userPrefs.copy(fontFamily = it)
        }
        sharedPrefs.getInt(preferredFontSize, 0).let {
            userPrefs = userPrefs.copy(fontSize = it)
        }
        sharedPrefs.getString(preferredColorScheme, null)?.let {
            userPrefs = userPrefs.copy(colorScheme = it)
        }
        sharedPrefs.getString(libraryFolderUri, null)?.let {
            userPrefs = userPrefs.copy(libraryUri = Uri.parse(it))
        }
        return userPrefs
    }

    override fun updateUserPrefs(prefs: UserPrefs) {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putString(preferredTheme, prefs.theme)
        editor.putString(preferredFontFamily, prefs.fontFamily)
        editor.putString(preferredColorScheme, prefs.colorScheme)
        editor.putInt(preferredFontSize, prefs.fontSize)
        editor.apply()
    }

    override fun saveAvailableFontFamilies(fonts: Set<String>) {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putStringSet(availableFontFamilies, fonts.toSet())
        editor.apply()
    }

    override fun getAvailableFontFamilies(): List<String> {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(availableFontFamilies, emptySet())?.toList()
            ?.sortedWith(String.CASE_INSENSITIVE_ORDER) ?: emptyList()

    }

    override fun saveAvailableFontSizes(fonts: Set<String>) {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putStringSet(availableFontSizes, fonts.toSet())
        editor.apply()
    }

    override fun getAvailableFontSizes(): List<String> {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(availableFontSizes, emptySet())?.toList()
            ?.sortedWith(String.CASE_INSENSITIVE_ORDER) ?: emptyList()
    }

    override fun saveAvailableThemes(themes: Set<String>) {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putStringSet(availableThemes, themes.toSet())
        editor.apply()

    }

    override fun getAvailableThemes(): List<String> {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(availableThemes, emptySet())?.toList()
            ?.sortedWith(String.CASE_INSENSITIVE_ORDER) ?: emptyList()
    }

    override fun saveAvailableColorSchemes(colorSchemes: Set<String>) {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putStringSet(availableColorSchemes, colorSchemes.toSet())
        editor.apply()

    }

    override fun getAvailableColorSchemes(): List<String> {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(availableColorSchemes, emptySet())?.toList()
            ?.sortedWith(String.CASE_INSENSITIVE_ORDER) ?: emptyList()
    }

    override fun saveLibraryFolderUri(uri: Uri) {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putString(libraryFolderUri, uri.toString())
        editor.apply()
    }

    override fun getLibraryFolderUri(): Uri? {
        val sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val uriString = sharedPrefs.getString(libraryFolderUri, null)
        return uriString?.let { Uri.parse(it) }
    }


    private companion object {
        const val fileName = "userPreferences"
        const val availableFontFamilies = "availableFontFamilies"
        const val availableFontSizes = "availableFontSizes"
        const val availableThemes = "availableThemes"
        const val availableColorSchemes = "availableColorSchemes"
        const val preferredTheme = "preferredTheme"
        const val preferredFontFamily = "preferredFontFamily"
        const val preferredFontSize = "preferredFontSize"
        const val preferredColorScheme = "preferredColorScheme"
        const val libraryFolderUri = "libraryFolderUri"

    }
}
package com.shamela.apptheme.data.sharedPrefs

import android.content.Context
import androidx.core.content.edit
import com.shamela.apptheme.domain.datasource.UserPrefsDataSource
import com.shamela.apptheme.domain.model.UserPrefs

class SharedPreferencesData(private val context: Context) : UserPrefsDataSource {
    override fun getUserPrefs(): UserPrefs {
        val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        var userPrefs = UserPrefs()
        sharedPrefs.getString(PREFERRED_THEME, "تلقائي")?.let {
            userPrefs = userPrefs.copy(theme = it)
        }
        sharedPrefs.getString(PREFERRED_FONT_FAMILY, "خط تَجَوَّل")?.let {
            userPrefs = userPrefs.copy(fontFamily = it)
        }
        sharedPrefs.getInt(PREFERRED_FONT_SIZE, -2).let {
            userPrefs = userPrefs.copy(fontSize = it)
        }
        sharedPrefs.getString(PREFERRED_COLOR_SCHEME, null)?.let {
            userPrefs = userPrefs.copy(colorScheme = it)
        }
        return userPrefs
    }

    override fun updateUserPrefs(prefs: UserPrefs) {
        val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit {
            putString(PREFERRED_THEME, prefs.theme)
            putString(PREFERRED_FONT_FAMILY, prefs.fontFamily)
            putString(PREFERRED_COLOR_SCHEME, prefs.colorScheme)
            putInt(PREFERRED_FONT_SIZE, prefs.fontSize)
        }
    }

    override fun saveAvailableFontFamilies(fonts: Set<String>) {
        val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit {
            putStringSet(AVAILABLE_FONT_FAMILIES, fonts.toSet())
        }
    }

    override fun getAvailableFontFamilies(): List<String> {
        val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(AVAILABLE_FONT_FAMILIES, emptySet())?.toList()
            ?.sortedWith(String.CASE_INSENSITIVE_ORDER) ?: emptyList()

    }

    override fun saveAvailableFontSizes(fonts: Set<String>) {
        val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit {
            putStringSet(AVAILABLE_FONT_SIZES, fonts.toSet())
        }
    }

    override fun getAvailableFontSizes(): List<String> {
        val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(AVAILABLE_FONT_SIZES, emptySet())?.toList()
            ?.sortedWith(String.CASE_INSENSITIVE_ORDER) ?: emptyList()
    }

    override fun saveAvailableThemes(themes: Set<String>) {
        val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit {
            putStringSet(AVAILABLE_THEMES, themes.toSet())
        }

    }

    override fun getAvailableThemes(): List<String> {
        val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(AVAILABLE_THEMES, emptySet())?.toList()
            ?.sortedWith(String.CASE_INSENSITIVE_ORDER) ?: emptyList()
    }

    override fun saveAvailableColorSchemes(colorSchemes: Set<String>) {
        val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit {
            putStringSet(AVAILABLE_COLOR_SCHEMES, colorSchemes.toSet())
        }

    }

    override fun getAvailableColorSchemes(): List<String> {
        val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(AVAILABLE_COLOR_SCHEMES, emptySet())?.toList()
            ?.sortedWith(String.CASE_INSENSITIVE_ORDER) ?: emptyList()
    }



    private companion object {
        const val FILE_NAME = "userPreferences"
        const val AVAILABLE_FONT_FAMILIES = "availableFontFamilies"
        const val AVAILABLE_FONT_SIZES = "availableFontSizes"
        const val AVAILABLE_THEMES = "availableThemes"
        const val AVAILABLE_COLOR_SCHEMES = "availableColorSchemes"
        const val PREFERRED_THEME = "preferredTheme"
        const val PREFERRED_FONT_FAMILY = "preferredFontFamily"
        const val PREFERRED_FONT_SIZE = "preferredFontSize"
        const val PREFERRED_COLOR_SCHEME = "preferredColorScheme"

    }
}
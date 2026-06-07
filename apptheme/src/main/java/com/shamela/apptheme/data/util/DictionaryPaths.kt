package com.shamela.apptheme.data.util

import android.content.Context
import java.io.File

/**
 * Single source of truth for the pre-built Arabic dictionary database.
 *
 * The dictionary ships *inside the APK* under `assets/[ASSET_NAME]` and is copied
 * once to internal storage on first use (SQLite can't open a compressed asset
 * directly). [DictionaryDatabase] resolves everything from a [Context] alone.
 *
 * On-disk copy (internal, app-private — survives without storage permissions and
 * is never null, unlike external dirs):
 *   /data/data/<pkg>/files/ShamelaDictionary/<FILE_NAME>
 */
object DictionaryPaths {
    const val DIR_NAME = "ShamelaDictionary"
    const val FILE_NAME = "shamela_dictionary.db"

    /** Path of the bundled DB inside the APK's merged assets. */
    const val ASSET_NAME = "dictionary/shamela_dictionary.db"

    /**
     * Bump whenever a new dictionary is bundled so the stale on-disk copy from a
     * previous app version is replaced. Written to [versionFile] after each copy.
     */
    const val BUNDLED_VERSION = 1

    fun dir(context: Context): File = File(context.filesDir, DIR_NAME)

    fun file(context: Context): File = File(dir(context), FILE_NAME)

    /** Marker holding the [BUNDLED_VERSION] that produced the current on-disk copy. */
    fun versionFile(context: Context): File = File(dir(context), "$FILE_NAME.version")

    /** True only when the dictionary file is present and non-empty. */
    fun exists(context: Context): Boolean = file(context).let { it.isFile && it.length() > 0L }
}

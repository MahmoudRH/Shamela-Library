package com.folioreader

import android.Manifest

/**
 * Created by mobisys on 10/4/2016.
 */
object Constants {
    const val SELECTED_CHAPTER_POSITION = "selected_chapter_position"
    const val TYPE = "type"
    const val CHAPTER_SELECTED = "chapter_selected"
    const val EPUB_FILE_PATH = "epub_file_path"

    const val HIGHLIGHT_SELECTED = "highlight_selected"
    const val BOOK_TITLE = "book_title"
    const val LOCALHOST = "http://127.0.0.1"
    const val DEFAULT_PORT_NUMBER = 8080
//    const val STREAMER_URL_TEMPLATE = "%s:%d/%s/"
    const val FONT_ANDADA = 1
    const val FONT_LATO = 2
    const val FONT_LORA = 3
    const val FONT_RALEWAY = 4
    const val DATE_FORMAT = "MMM dd, yyyy | HH:mm"
    const val WRITE_EXTERNAL_STORAGE_REQUEST = 102
    val writeExternalStoragePerms: Array<String>
        get() = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
}
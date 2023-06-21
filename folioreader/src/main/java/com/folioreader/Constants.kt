package com.folioreader

import android.Manifest

/**
 * Created by mobisys on 10/4/2016.
 */
object Constants {
    const val PUBLICATION = "PUBLICATION"
    const val SELECTED_CHAPTER_POSITION = "selected_chapter_position"
    const val TYPE = "type"
    const val CHAPTER_SELECTED = "chapter_selected"
    const val HIGHLIGHT_SELECTED = "highlight_selected"
    const val BOOK_TITLE = "book_title"
    const val LOCALHOST = "http://127.0.0.1"
    const val DEFAULT_PORT_NUMBER = 8080
    const val STREAMER_URL_TEMPLATE = "%s:%d/%s/"
    const val DEFAULT_STREAMER_URL = LOCALHOST + ":" + DEFAULT_PORT_NUMBER + "/"
    const val SELECTED_WORD = "selected_word"
    const val DICTIONARY_BASE_URL = "https://api.pearson.com/v2/dictionaries/entries?headword="
    const val WIKIPEDIA_API_URL =
        "https://en.wikipedia.org/w/api.php?action=opensearch&namespace=0&format=json&search="
    const val FONT_ANDADA = 1
    const val FONT_LATO = 2
    const val FONT_LORA = 3
    const val FONT_RALEWAY = 4
    const val DATE_FORMAT = "MMM dd, yyyy | HH:mm"
    const val ASSET = "file:///android_asset/"
    const val WRITE_EXTERNAL_STORAGE_REQUEST = 102
    const val CHAPTER_ID = "id"
    const val HREF = "href"
    val writeExternalStoragePerms: Array<String>
        get() = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
}
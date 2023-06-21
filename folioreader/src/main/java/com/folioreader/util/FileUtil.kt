package com.folioreader.util


object FileUtil {
    fun getEpubFilename(path: String) = path.substring(path.lastIndexOf('/') + 1, path.indexOf(".epub"))
}
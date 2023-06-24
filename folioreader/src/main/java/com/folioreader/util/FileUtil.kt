package com.folioreader.util


object FileUtil {
    fun getEpubFilename(path: String) =  try {
        path.substring(path.lastIndexOf('/') + 1, path.indexOf(".epub"))
    }catch (e:IndexOutOfBoundsException){
        "الشاملة"
    }
}
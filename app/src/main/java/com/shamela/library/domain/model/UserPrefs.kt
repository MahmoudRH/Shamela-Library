package com.shamela.library.domain.model

import android.net.Uri

data class UserPrefs(
    val theme:String = "تلقائي",
    val colorScheme:String = "أزرق",
    val fontFamily:String = "خط النسخ",
    val fontSize:Int = -2,
    val libraryUri:Uri? = null,
    )

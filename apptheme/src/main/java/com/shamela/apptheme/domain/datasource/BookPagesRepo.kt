package com.shamela.apptheme.domain.datasource

import com.shamela.apptheme.domain.model.BookPage

interface BookPagesRepo {
    suspend fun insertPage(page: BookPage):Boolean
    suspend fun searchBook(bookId:String, query:String):List<BookPage>
    suspend fun searchCategory(category:String, query:String):List<BookPage>
    suspend fun searchLibrary(query:String):List<BookPage>
}
package com.shamela.apptheme.domain.usecases.bookPages

import com.shamela.apptheme.domain.datasource.BookPagesRepo
import com.shamela.apptheme.domain.model.BookPage

class SearchBook(private val repo: BookPagesRepo) {
    suspend operator fun invoke(bookId:String,query:String): List<BookPage> {
        return repo.searchBook(bookId ,query )
    }
}
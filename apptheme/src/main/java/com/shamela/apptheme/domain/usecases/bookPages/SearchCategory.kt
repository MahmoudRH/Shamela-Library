package com.shamela.apptheme.domain.usecases.bookPages

import com.shamela.apptheme.domain.datasource.BookPagesRepo
import com.shamela.apptheme.domain.model.BookPage

class SearchCategory(private val repo: BookPagesRepo) {
    suspend operator fun invoke(category:String,query:String): List<BookPage> {
        return repo.searchCategory(category ,query )
    }
}
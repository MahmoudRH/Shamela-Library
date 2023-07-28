package com.shamela.apptheme.domain.usecases.bookPages

import com.shamela.apptheme.domain.datasource.BookPagesRepo
import com.shamela.apptheme.domain.model.BookPage

class SearchLibrary(private val repo: BookPagesRepo) {
    suspend operator fun invoke(query:String): List<BookPage> {
        return repo.searchLibrary(query )
    }
}
package com.shamela.apptheme.domain.usecases.bookPages

import com.shamela.apptheme.domain.datasource.BookPagesRepo
import com.shamela.apptheme.domain.model.BookPage

class InsertBookPage(private val repo: BookPagesRepo) {
    suspend operator fun invoke(page: BookPage): Boolean {
       return repo.insertPage(page)
    }
}
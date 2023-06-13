package com.shamela.library.domain.usecases.books

import android.net.Uri
import com.shamela.library.domain.repo.BooksRepository

class GetDownloadUri constructor(private val repo: BooksRepository) {
    suspend operator fun invoke(categoryName: String, bookName: String): Uri? {
        return repo.getDownloadLink(categoryName = categoryName, bookName = bookName)
    }
}
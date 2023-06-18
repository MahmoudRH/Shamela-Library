package com.shamela.library.domain.usecases.books

import com.shamela.library.domain.model.Category
import com.shamela.library.domain.repo.BooksRepository
import kotlinx.coroutines.flow.Flow

class GetAllCategories(private val repo: BooksRepository) {
    operator fun invoke(): Flow<Category> {
        return repo.getCategories()
    }
}
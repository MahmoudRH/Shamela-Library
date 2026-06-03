package com.shamela.library.domain.util

import com.shamela.library.domain.model.Book

object BooksGroupingUtil {
    fun groupByFirstChar(books: List<Book>): Map<Char, List<Book>> {
        return books.sortedBy { it.title }.groupBy { it.title.firstOrNull() ?: '-' }
    }
}

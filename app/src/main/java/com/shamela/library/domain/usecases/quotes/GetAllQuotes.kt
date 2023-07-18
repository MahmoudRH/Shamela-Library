package com.shamela.library.domain.usecases.quotes

import com.shamela.library.data.local.db.QuotesDao
import com.shamela.library.domain.model.Quote
import kotlinx.coroutines.flow.Flow

class GetAllQuotes(private val dao:QuotesDao) {
    operator fun invoke(): Flow<List<Quote>> {
        return dao.getAllQuotes()
    }
}
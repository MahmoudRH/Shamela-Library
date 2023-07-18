package com.shamela.library.domain.usecases.quotes

import com.shamela.library.data.local.db.QuotesDao
import com.shamela.library.domain.model.Quote

class SaveQuote(private val dao:QuotesDao) {
    suspend operator fun invoke(quote: Quote){
        dao.insert(quote)
    }
}
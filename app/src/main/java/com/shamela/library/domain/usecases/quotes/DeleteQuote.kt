package com.shamela.library.domain.usecases.quotes

import com.shamela.library.data.local.db.QuotesDao

class DeleteQuote(private val dao:QuotesDao) {
    suspend operator fun invoke(quoteId:String){
        dao.delete(quoteId)
    }
}
package com.shamela.library.domain.usecases.quotes

import com.shamela.library.data.local.db.QuotesDao

data class QuotesUseCases(
    private val dao: QuotesDao,
    val saveQuote: SaveQuote = SaveQuote(dao),
    val deleteQuote: DeleteQuote = DeleteQuote(dao),
    val getAllQuotes: GetAllQuotes = GetAllQuotes(dao),
)

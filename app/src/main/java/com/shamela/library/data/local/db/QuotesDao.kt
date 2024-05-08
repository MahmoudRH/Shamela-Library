package com.shamela.library.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shamela.library.domain.model.Quote
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quote: Quote)

    @Query("DELETE FROM Quote WHERE quoteId = :qId")
    suspend fun delete(qId: String)

    @Query("SELECT * FROM Quote")
    fun getAllQuotes(): Flow<List<Quote>>

}
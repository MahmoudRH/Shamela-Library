package com.shamela.library.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shamela.library.domain.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BooksDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(book: Book)

    @Query("DELETE FROM downloadedBooks WHERE id = :bookId")
    suspend fun delete(bookId: String)

    @Query("UPDATE downloadedBooks SET isFavorite = :isFavorite WHERE id = :bookId")
    suspend fun updateBook(bookId: String, isFavorite: Int)

    @Query("SELECT * FROM downloadedBooks")
    fun getDownloadedBooks(): Flow<List<Book>>

    @Query("SELECT * FROM downloadedBooks WHERE isFavorite = 1  ")
    fun getFavoriteBooks(): Flow<List<Book>>


}
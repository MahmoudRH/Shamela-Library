package com.shamela.library.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "DownloadedBooks",
)
data class Book(
    @PrimaryKey val id: String,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("author") val author: String,
    @ColumnInfo("pageCount") val pageCount: Int,
    @ColumnInfo("categoryName") val categoryName: String,
    @ColumnInfo("isFavorite") val isFavorite: Boolean = false,
){
    fun getPath(){
        return
    }
}
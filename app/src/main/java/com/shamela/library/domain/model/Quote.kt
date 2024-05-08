package com.shamela.library.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Quote", foreignKeys = [ForeignKey(entity = Book::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE)])
data class Quote(
    @ColumnInfo val text: String,
    @ColumnInfo val pageIndex: Int,
    @ColumnInfo val pageHref: String,
    @ColumnInfo val bookName: String,
    @ColumnInfo val bookId: String,
    @PrimaryKey val quoteId: Int = text.hashCode(),
)

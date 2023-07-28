package com.shamela.apptheme.domain.model

data class BookPage(
    val href:String,
    val content:String,
    val bookId:String,
    val category:String,
){
    var id:String
        private set
    init {
        id = bookId+href
    }
    companion object{
        const val TABLE_NAME = "BookPages_fts"
        const val COL_ID = "_id"
        const val COL_HREF = "href"
        const val COL_CONTENT = "content"
        const val COL_CATEGORY = "category"
        const val COL_BOOK_ID = "book_id"
        const val CREATE_TABLE = "CREATE VIRTUAL TABLE $TABLE_NAME USING fts5 ( " +
                "$COL_ID UNINDEXED, " +
                "$COL_HREF UNINDEXED, " +
                "$COL_CATEGORY UNINDEXED, " +
                "$COL_BOOK_ID UNINDEXED, " +
                "$COL_CONTENT );"
    }
}

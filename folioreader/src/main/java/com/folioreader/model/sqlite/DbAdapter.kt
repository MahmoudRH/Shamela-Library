package com.folioreader.model.sqlite

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase

class DbAdapter {
    fun deleteAll(table: String?): Boolean {
        return mDatabase!!.delete(table, null, null) > 0
    }

    fun deleteAll(table: String?, whereClause: String, whereArgs: Array<String?>?): Boolean {
        return mDatabase!!.delete(table, "$whereClause=?", whereArgs) > 0
    }

    fun getAll(
        table: String?, projection: Array<String?>?, selection: String?,
        selectionArgs: Array<String?>?, orderBy: String?
    ): Cursor {
        return mDatabase!!.query(table, projection, selection, selectionArgs, null, null, orderBy)
    }

    fun getAll(table: String?): Cursor {
        return getAll(table, null, null, null, null)
    }

    @JvmOverloads
    @Throws(SQLException::class)
    operator fun get(
        table: String?,
        id: Long,
        projection: Array<String?>? = null,
        key: String = FolioDatabaseHelper.Companion.KEY_ID
    ): Cursor {
        return mDatabase!!.query(
            table, projection,
            "$key=?", arrayOf(id.toString()), null, null, null, null
        )
    }

    fun getMaxId(tableName: String, key: String): Cursor {
        return mDatabase!!.rawQuery("SELECT MAX($key) FROM $tableName", null)
    }

    companion object {
        private const val TAG = "DBAdapter"
        var mDatabase: SQLiteDatabase? = null
        fun initialize(mContext: Context?) {
            mDatabase = FolioDatabaseHelper.Companion.getInstance(mContext)?.myWritableDatabase
        }

        fun terminate() {
            FolioDatabaseHelper.Companion.clearInstance()
        }

        fun insert(table: String?, contentValues: ContentValues?): Boolean {
            return mDatabase!!.insert(table, null, contentValues) > 0
        }

        fun update(
            table: String?,
            key: String,
            value: String,
            contentValues: ContentValues?
        ): Boolean {
            return mDatabase!!.update(table, contentValues, "$key=?", arrayOf(value)) > 0
        }

        fun getHighLightsForBookId(bookId: String?): Cursor {
            return mDatabase!!.rawQuery(
                "SELECT * FROM " + HighLightTable.TABLE_NAME + " WHERE " + HighLightTable.COL_BOOK_ID + " = \"" + bookId + "\"",
                null
            )
        }

        @Throws(SQLException::class)
        fun getAllByKey(
            table: String?,
            projection: Array<String?>?,
            key: String,
            value: String
        ): Cursor {
            return mDatabase!!.query(
                table, projection,
                "$key=?", arrayOf(value), null, null, null, null
            )
        }

        fun deleteById(table: String?, key: String, value: String): Boolean {
            return mDatabase!!.delete(table, "$key=?", arrayOf(value)) > 0
        }

        fun saveHighLight(highlightContentValues: ContentValues?): Long {
            return mDatabase!!.insert(HighLightTable.TABLE_NAME, null, highlightContentValues)
        }

        fun updateHighLight(highlightContentValues: ContentValues?, id: String): Boolean {
            return mDatabase!!.update(
                HighLightTable.TABLE_NAME,
                highlightContentValues,
                HighLightTable.ID + " = " + id,
                null
            ) > 0
        }

        fun getHighlightsForPageId(query: String?, pageId: String?): Cursor {
            return mDatabase!!.rawQuery(query, null)
        }

        @SuppressLint("Range")
        fun getIdForQuery(query: String?): Int {
            val c = mDatabase!!.rawQuery(query, null)
            var id = -1
            while (c.moveToNext()) {
                id = c.getInt(c.getColumnIndex(HighLightTable.ID))
            }
            c.close()
            return id
        }

        fun getHighlightsForId(id: Int): Cursor {
            return mDatabase!!.rawQuery(
                "SELECT * FROM " + HighLightTable.TABLE_NAME + " WHERE " + HighLightTable.ID + " = \"" + id + "\"",
                null
            )
        }
    }
}
package com.folioreader.model.sqlite

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.text.TextUtils
import android.util.Log
import com.folioreader.Constants
import com.folioreader.model.HighLight
import com.folioreader.model.HighlightImpl
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object HighLightTable {
    const val TABLE_NAME = "highlight_table"
    const val ID = "_id"
    const val COL_BOOK_ID = "bookId"
    private const val COL_CONTENT = "content"
    private const val COL_DATE = "date"
    private const val COL_TYPE = "type"
    private const val COL_PAGE_NUMBER = "page_number"
    private const val COL_PAGE_ID = "pageId"
    private const val COL_RANGY = "rangy"
    private const val COL_NOTE = "note"
    private const val COL_UUID = "uuid"
    const val SQL_CREATE = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + COL_BOOK_ID + " TEXT" + ","
            + COL_CONTENT + " TEXT" + ","
            + COL_DATE + " TEXT" + ","
            + COL_TYPE + " TEXT" + ","
            + COL_PAGE_NUMBER + " INTEGER" + ","
            + COL_PAGE_ID + " TEXT" + ","
            + COL_RANGY + " TEXT" + ","
            + COL_UUID + " TEXT" + ","
            + COL_NOTE + " TEXT" + ")")
    const val SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME
    val TAG = HighLightTable::class.java.simpleName
    fun getHighlightContentValues(highLight: HighLight?): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(COL_BOOK_ID, highLight?.bookId)
        contentValues.put(COL_CONTENT, highLight?.content)
        contentValues.put(COL_DATE, getDateTimeString(highLight?.date))
        contentValues.put(COL_TYPE, highLight?.type)
        contentValues.put(COL_PAGE_NUMBER, highLight?.pageNumber)
        contentValues.put(COL_PAGE_ID, highLight?.pageId)
        contentValues.put(COL_RANGY, highLight?.rangy)
        contentValues.put(COL_NOTE, highLight?.note)
        contentValues.put(COL_UUID, highLight?.uUID)
        return contentValues
    }

    @SuppressLint("Range")
    fun getAllHighlights(bookId: String?): ArrayList<HighlightImpl> {
        val highlights = ArrayList<HighlightImpl>()
        val highlightCursor: Cursor = DbAdapter.Companion.getHighLightsForBookId(bookId)
        while (highlightCursor.moveToNext()) {
            highlights.add(
                HighlightImpl(
                    highlightCursor.getInt(highlightCursor.getColumnIndex(ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_BOOK_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_CONTENT)),
                    getDateTime(highlightCursor.getString(highlightCursor.getColumnIndex(COL_DATE))),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_TYPE)),
                    highlightCursor.getInt(highlightCursor.getColumnIndex(COL_PAGE_NUMBER)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_PAGE_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_RANGY)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_NOTE)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_UUID))
                )
            )
        }
        return highlights
    }

    @SuppressLint("Range")
    fun getHighlightId(id: Int): HighlightImpl {
        val highlightCursor: Cursor = DbAdapter.Companion.getHighlightsForId(id)
        var highlightImpl = HighlightImpl()
        while (highlightCursor.moveToNext()) {
            highlightImpl = HighlightImpl(
                highlightCursor.getInt(highlightCursor.getColumnIndex(ID)),
                highlightCursor.getString(highlightCursor.getColumnIndex(COL_BOOK_ID)),
                highlightCursor.getString(highlightCursor.getColumnIndex(COL_CONTENT)),
                getDateTime(highlightCursor.getString(highlightCursor.getColumnIndex(COL_DATE))),
                highlightCursor.getString(highlightCursor.getColumnIndex(COL_TYPE)),
                highlightCursor.getInt(highlightCursor.getColumnIndex(COL_PAGE_NUMBER)),
                highlightCursor.getString(highlightCursor.getColumnIndex(COL_PAGE_ID)),
                highlightCursor.getString(highlightCursor.getColumnIndex(COL_RANGY)),
                highlightCursor.getString(highlightCursor.getColumnIndex(COL_NOTE)),
                highlightCursor.getString(highlightCursor.getColumnIndex(COL_UUID))
            )
        }
        return highlightImpl
    }

    fun insertHighlight(highlightImpl: HighlightImpl): Long {
        highlightImpl.uUID= UUID.randomUUID().toString()
        return DbAdapter.Companion.saveHighLight(getHighlightContentValues(highlightImpl))
    }

    fun deleteHighlight(rangy: String): Boolean {
        val query =
            "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\""
        val id: Int = DbAdapter.Companion.getIdForQuery(query)
        return id != -1 && deleteHighlight(id)
    }

    fun deleteHighlight(highlightId: Int): Boolean {
        return DbAdapter.Companion.deleteById(TABLE_NAME, ID, highlightId.toString())
    }

    @SuppressLint("Range")
    fun getHighlightsForPageId(pageId: String): List<String> {
        val query =
            "SELECT " + COL_RANGY + " FROM " + TABLE_NAME + " WHERE " + COL_PAGE_ID + " = \"" + pageId + "\""
        val c: Cursor = DbAdapter.Companion.getHighlightsForPageId(query, pageId)
        val rangyList: MutableList<String> = ArrayList()
        while (c.moveToNext()) {
            rangyList.add(c.getString(c.getColumnIndex(COL_RANGY)))
        }
        c.close()
        return rangyList
    }

    fun updateHighlight(highlightImpl: HighlightImpl?): Boolean {
        return DbAdapter.Companion.updateHighLight(
            getHighlightContentValues(highlightImpl),
            highlightImpl?.id.toString()
        )
    }

    fun getDateTimeString(date: Date?): String {
        val dateFormat = SimpleDateFormat(
            Constants.DATE_FORMAT, Locale.getDefault()
        )
        return dateFormat.format(date)
    }

    fun getDateTime(date: String?): Date {
        val dateFormat = SimpleDateFormat(
            Constants.DATE_FORMAT, Locale.getDefault()
        )
        var date1 = Date()
        try {
            date1 = dateFormat.parse(date)
        } catch (e: ParseException) {
            Log.e(TAG, "Date parsing failed", e)
        }
        return date1
    }

    fun updateHighlightStyle(rangy: String, style: String): HighlightImpl? {
        val query =
            "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\""
        val id: Int = DbAdapter.Companion.getIdForQuery(query)
        return if (id != -1 && update(
                id,
                updateRangy(rangy, style),
                style.replace("highlight_", "")
            )
        ) {
            getHighlightId(id)
        } else null
    }

    fun getHighlightForRangy(rangy: String): HighlightImpl {
        val query =
            "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\""
        return getHighlightId(DbAdapter.Companion.getIdForQuery(query))
    }

    private fun updateRangy(rangy: String, style: String): String {
        /*Pattern p = Pattern.compile("\\highlight_\\w+");
        Matcher m = p.matcher(rangy);
        return m.replaceAll(style);*/
        val s = rangy.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val builder = StringBuilder()
        for (p in s) {
            if (TextUtils.isDigitsOnly(p)) {
                builder.append(p)
                builder.append('$')
            } else {
                builder.append(style)
                builder.append('$')
            }
        }
        return builder.toString()
    }

    private fun update(id: Int, s: String, color: String): Boolean {
        val highlightImpl = getHighlightId(id)
        highlightImpl.rangy = s
        highlightImpl.type = color
        return DbAdapter.Companion.updateHighLight(
            getHighlightContentValues(highlightImpl),
            id.toString()
        )
    }

    fun saveHighlightIfNotExists(highLight: HighLight) {
        val query =
            "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_UUID + " = \"" + highLight.uUID + "\""
        val id: Int = DbAdapter.Companion.getIdForQuery(query)
        if (id == -1) {
            DbAdapter.Companion.saveHighLight(getHighlightContentValues(highLight))
        }
    }
}
package com.folioreader.model.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.util.Locale

/**
 * @author gautam chibde on 28/6/17.
 */
class DictionaryTable(context: Context?) {
    private val database: SQLiteDatabase

    init {
        val dbHelper = FolioDatabaseHelper(context)
        database = dbHelper.writableDatabase
    }

    fun insertWord(word: String?, meaning: String?): Boolean {
        val values = ContentValues()
        values.put(WORD, word)
        values.put(MEANING, meaning)
        return database.insert(TABLE_NAME, null, values) > 0
    }

    fun insert(map: Map<String, String?>) {
        database.beginTransaction()
        for (key in map.keys) {
            insertWord(key.lowercase(Locale.getDefault()), map[key])
        }
        database.setTransactionSuccessful()
        database.endTransaction()
    }

    fun getMeaningForWord(word: String): String? {
        val c = database.rawQuery("SELECT * FROM "
                + TABLE_NAME +
                " WHERE " + WORD + " = \"" + word.trim { it <= ' ' } + "\"", null)
        if (c.moveToFirst()) {
            val toRetuen = c.getString(2)
            c.close()
            return toRetuen
        }
        c.close()
        return null
    }

    fun getMeaning(word: String): List<String> {
        val words: MutableList<String> = ArrayList()
        val meaning = getMeaningForWord(word)
        return if (meaning != null) {
            words.add(meaning)
            words
        } else {
            getProbableCombinations(word)
        }
    }

    private fun getProbableCombinations(word: String): List<String> {
        val combinations: MutableList<String> = ArrayList()
        for (i in 1..3) {
            val m = getMeaningForWord(word.substring(0, word.length - i))
            if (m != null) {
                combinations.add(m)
            }
        }
        return combinations
    }

    companion object {
        const val TABLE_NAME = "dictionary_table"
        const val ID = "_id"
        const val WORD = "word"
        const val MEANING = "meaning"
        const val SQL_CREATE = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
                + WORD + " TEXT" + ","
                + MEANING + " TEXT" + ")")
        const val SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME
    }
}
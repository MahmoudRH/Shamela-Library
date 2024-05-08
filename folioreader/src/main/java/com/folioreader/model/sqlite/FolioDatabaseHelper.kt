package com.folioreader.model.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class FolioDatabaseHelper(private val mContext: Context?) : SQLiteOpenHelper(
    mContext, DATABASE_NAME, null, DATABASE_VERSION
) {
    val myWritableDatabase: SQLiteDatabase?
        get() {
            if (myWritableDb == null || !myWritableDb!!.isOpen) {
                myWritableDb = this.writableDatabase
            }
            return myWritableDb
        }

    override fun close() {
        super.close()
        if (myWritableDb != null) {
            myWritableDb!!.close()
            myWritableDb = null
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("create table highlight", "****" + HighLightTable.SQL_CREATE)
        db.execSQL(HighLightTable.SQL_CREATE)
    }

    override fun onUpgrade(
        db: SQLiteDatabase, oldVersion: Int,
        newVersion: Int
    ) {
        /* PROTECTED REGION ID(DatabaseUpdate) ENABLED START */

        // TODO Implement your database update functionality here and remove the
        // following method call!
        //onUpgradeDropTables(db);
        //onCreate(db);
        resetAllPreferences(mContext)

        /* PROTECTED REGION END */
    }

    /**
     * This basic upgrade functionality will destroy all old data on upgrade
     */
    private fun onUpgradeDropTables(db: SQLiteDatabase) {}

    /**
     * Resets all shared preferences
     *
     * @param context
     */
    private fun resetAllPreferences(context: Context?) {}

    companion object {
        private const val TAG = "SQLiteOpenHelper"
        private var mInstance: FolioDatabaseHelper? = null
        private var myWritableDb: SQLiteDatabase? = null
        const val DATABASE_NAME = "FolioReader.db"
        private const val DATABASE_VERSION = 2
        const val KEY_ID = "_id"
        fun getInstance(context: Context?): FolioDatabaseHelper? {
            if (mInstance == null) {
                mInstance = FolioDatabaseHelper(context)
            }
            return mInstance
        }

        fun clearInstance() {
            mInstance = null
        }
    }
}
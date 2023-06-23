package com.folioreader.util

import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by PC on 6/9/2016.
 */
object SharedPreferenceUtil {

    fun putSharedPreferencesString(context: Context?, key: String?, `val`: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putString(key, `val`)
        edit.apply()
    }
    fun getSharedPreferencesString(
        context: Context?,
        key: String?,
        defaultValue: String?
    ): String? {
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, defaultValue)
    }
}
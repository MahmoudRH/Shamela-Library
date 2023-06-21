package com.folioreader.util

import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by PC on 6/9/2016.
 */
object SharedPreferenceUtil {
    const val SENT_TOKEN_TO_SERVER = "sentTokenToServer"
    const val REGISTRATION_COMPLETE = "registrationComplete"
    fun putSharedPreferencesInt(context: Context?, key: String?, value: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putInt(key, value)
        edit.commit()
    }

    fun putSharedPreferencesBoolean(context: Context?, key: String?, `val`: Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putBoolean(key, `val`)
        edit.commit()
    }

    fun putSharedPreferencesString(context: Context?, key: String?, `val`: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putString(key, `val`)
        edit.commit()
    }

    fun putSharedPreferencesStringSet(context: Context?, key: String?, `val`: Set<String?>?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putStringSet(key, `val`)
        editor.commit()
    }

    fun putSharedPreferencesFloat(context: Context?, key: String?, `val`: Float) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putFloat(key, `val`)
        edit.commit()
    }

    fun putSharedPreferencesLong(context: Context?, key: String?, `val`: Long) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putLong(key, `val`)
        edit.commit()
    }

    fun getSharedPreferencesLong(context: Context?, key: String?, defaultValue: Long): Long {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getLong(key, defaultValue)
    }

    fun getSharedPreferencesFloat(context: Context?, key: String?, defaultValue: Float): Float {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getFloat(key, defaultValue)
    }

    fun getSharedPreferencesString(
        context: Context?,
        key: String?,
        defaultValue: String?
    ): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, defaultValue)
    }

    fun getSharedPreferencesStringSet(
        context: Context?,
        key: String?,
        defaultValue: Set<String?>?
    ): Set<String>? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getStringSet(key, defaultValue)
    }

    fun getSharedPreferencesInt(context: Context?, key: String?, defaultValue: Int): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getInt(key, defaultValue)
    }

    fun getSharedPreferencesBoolean(
        context: Context?,
        key: String?,
        defaultValue: Boolean
    ): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(key, defaultValue)
    }

    fun removeSharedPreferencesKey(context: Context?, key: String?): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.remove(key)
        return editor.commit()
    }
}
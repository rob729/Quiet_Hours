package com.rob729.quiethours.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object StoreSession {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var appSharedPrefs: SharedPreferences
    fun init(context: Context) {
        sharedPreferences =
            context.getSharedPreferences(AppConstants.ACTIVE_PROFILE_NAME, Context.MODE_PRIVATE)
        appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun writeString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    fun readString(key: String): String {
        return sharedPreferences.getString(key, " ").toString()
    }
    fun writeInt(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor?.putInt(key, value)
        editor?.apply()
    }

    fun readInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun setNightMode(value: Boolean) {
        val editor = appSharedPrefs.edit()
        editor?.putBoolean(AppConstants.NIGHT_MODE, value)
        editor?.apply()
    }

    fun getNightMode(): Boolean {
        return appSharedPrefs.getBoolean(AppConstants.NIGHT_MODE, false)
    }

    fun writeBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor?.putBoolean(key, value)
        editor?.apply()
    }

    fun readBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun writeLong(key: String, value: Long) {
        val editor = sharedPreferences.edit()
        editor?.putLong(key, value)
        editor?.apply()
    }

    fun readLong(key: String): Long {
        return sharedPreferences.getLong(key, 0)
    }
}
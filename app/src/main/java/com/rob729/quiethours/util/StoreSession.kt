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
        sharedPreferences.edit().let {
            it.putString(key, value)
            it.apply()
        }
    }

    fun readString(key: String): String {
        return sharedPreferences.getString(key, " ").toString()
    }
    fun writeInt(key: String, value: Int) {
        sharedPreferences.edit().let {
            it.putInt(key, value)
            it.apply()
        }
    }

    fun readInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun setNightMode(value: Boolean) {
        appSharedPrefs.edit().let {
            it.putBoolean(AppConstants.NIGHT_MODE, value)
            it.apply()
        }
    }

    fun getNightMode(): Boolean {
        return appSharedPrefs.getBoolean(AppConstants.NIGHT_MODE, false)
    }

    fun writeBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().let {
            it.putBoolean(key, value)
            it.apply()
        }
    }

    fun readBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun writeLong(key: String, value: Long) {
        sharedPreferences.edit().let {
            it.putLong(key, value)
            it.apply()
        }
    }

    fun readLong(key: String): Long {
        return sharedPreferences.getLong(key, 0)
    }
}
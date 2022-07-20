package com.silentrald.parkingrssi

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val PREFERENCES = "parkingrssi"
    private lateinit var sp: SharedPreferences

    init {
        sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    }

    fun setInt(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, default: Int): Int {
        return sp.getInt(key, default)
    }
}
package com.jaisoft.cootrapeldarapp
import android.content.Context
import android.content.SharedPreferences

class CookieHandler(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("CookiePrefs", Context.MODE_PRIVATE)

    fun saveCookies(url: String, cookies: String) {
        val editor = sharedPreferences.edit()
        editor.putString(url, cookies)
        editor.apply()
    }

    fun getCookies(url: String): String? {
        return sharedPreferences.getString(url, null)
    }
}

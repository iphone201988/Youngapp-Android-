package com.tech.young.base.local

import android.content.SharedPreferences
import com.tech.young.data.model.Verification2faApiResponse
import com.google.gson.Gson
import javax.inject.Inject

class SharedPrefManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    object KEY {
        const val IS_FIRST = "is_first"
        const val USER_ID  = "USER_ID"
        const val TOKEN = "token"

        const val SUBSCRIBED = "subscribed"


    }

    fun setLoginData(isFirst: Verification2faApiResponse.Data) {
        val gson = Gson()
        val json = gson.toJson(isFirst)
        val editor = sharedPreferences.edit()
        editor.putString(KEY.IS_FIRST, json)
        editor.apply()
    }

    fun getLoginData(): Verification2faApiResponse.Data? {
        val gson = Gson()
        val json: String? = sharedPreferences.getString(KEY.IS_FIRST, null)
        return if (!json.isNullOrEmpty()) {
            gson.fromJson(json, Verification2faApiResponse.Data::class.java)
        } else {
            null
        }
    }

    fun setToken(isFirst: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY.TOKEN, isFirst)
        editor.apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY.TOKEN, "")
    }

    fun saveUserId(userId: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY.USER_ID, userId)
        editor.apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(KEY.USER_ID, null)
    }
    // Save subscriber value
    // Save subscriber value safely
    fun setSubscribed(isSubscribed: Boolean?) {
        sharedPreferences.edit().putBoolean(KEY.SUBSCRIBED, isSubscribed ?: false)
    }

    // Get subscriber value (default false)
    fun isSubscribed(): Boolean {
        return sharedPreferences.getBoolean(KEY.SUBSCRIBED, false)
    }


    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
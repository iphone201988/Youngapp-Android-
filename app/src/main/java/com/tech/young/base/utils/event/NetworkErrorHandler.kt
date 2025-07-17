package com.tech.young.base.utils.event

import android.app.Application
import com.tech.young.R
import okhttp3.ResponseBody
import org.json.JSONObject

class NetworkErrorHandler(val context: Application) {

    fun getErrorMessage(errorBody: ResponseBody?): String? {
        return try {
            var errMsg = context.getString(R.string.error_found)
            if (errorBody != null) {
                val jsonObject = JSONObject(errorBody.string())
                errMsg = jsonObject.getString("message")
            }
            errMsg
        } catch (e: Exception) {
            errorBody.toString()
        }
    }
}
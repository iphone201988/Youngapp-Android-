package com.tech.young.utils

import android.util.Base64
import android.util.Log
import com.tech.young.data.api.DiditApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DiditService {

    private const val CLIENT_ID = "QDPf650HjF-NyD4ARVlA4w"
    private const val CLIENT_SECRET = "oVFp1ZwgNE_uNsF9b0Rz3hhbGg05zS-gd3boekX2aks"
    private const val BASE_AUTH_URL = "https://apx.didit.me/"
    private const val BASE_VERIFICATION_URL = "https://verification.didit.me/"

    private var accessToken: String? = null
    private var tokenExpiry: Long? = null

    private val authRetrofit = Retrofit.Builder()
        .baseUrl(BASE_AUTH_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val verificationRetrofit = Retrofit.Builder()
        .baseUrl(BASE_VERIFICATION_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authApi = authRetrofit.create(DiditApi::class.java)
    private val verificationApi = verificationRetrofit.create(DiditApi::class.java)

    private suspend fun getValidToken(): String {
        val now = System.currentTimeMillis() / 1000
        if (accessToken != null && tokenExpiry != null && now < tokenExpiry!!) {
            Log.d("DiditService", "Reusing valid token: $accessToken")
            return accessToken!!
        }

        Log.d("DiditService", "Fetching new token...")

        val creds = "$CLIENT_ID:$CLIENT_SECRET"
        val base64Creds = Base64.encodeToString(creds.toByteArray(), Base64.NO_WRAP)
        val response = authApi.authenticate("Basic $base64Creds")

        if (response.isSuccessful) {
            val body = response.body()!!
            accessToken = body.accessToken
            tokenExpiry = now + body.expiresIn

            Log.d("DiditService", "New token acquired: $accessToken")
            Log.d("DiditService", "Token expires at epoch: $tokenExpiry")

            return accessToken!!
        } else {
            val error = response.errorBody()?.string()
            Log.e("DiditService", "Authentication failed: $error")
            throw Exception("Auth failed: $error")
        }
    }

    suspend fun createVerificationSession(vendorId: String): Map<String, Any> {
        Log.d("DiditService", "Creating verification session for vendor ID: $vendorId")

        val token = getValidToken()
        val requestBody = mapOf("vendor_data" to vendorId)

        Log.d("DiditService", "Sending session creation request with token: $token")

        val response = verificationApi.createSession("Bearer $token", requestBody)

        return if (response.isSuccessful) {
            val responseBody = response.body()!!
            Log.d("DiditService", "Session created successfully: $responseBody")
            responseBody
        } else {
            val error = response.errorBody()?.string()
            Log.e("DiditService", "Session creation failed: $error")
            throw Exception("Session creation failed: $error")
        }
    }
}


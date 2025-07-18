package com.tech.young.data.api

import com.tech.young.data.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface DiditApi {

    @FormUrlEncoded
    @POST("auth/v2/token/")
    suspend fun authenticate(
        @Header("Authorization") authHeader: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Response<AuthResponse>

    @POST("v1/session")
    suspend fun createSession(
        @Header("Authorization") token: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, Any>>
}
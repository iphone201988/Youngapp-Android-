package com.tech.young.data.api

import com.google.gson.JsonObject
import com.tech.young.data.IndexQuote
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url


interface ApiService {
//    @Header("Authorization") token: String,


    @POST
    suspend fun apiForRawBody(
        @Body data: HashMap<String, Any>, @Url url: String
    ): Response<JsonObject>

    @POST
    suspend fun apiPostForRawBody(
        @Header("Authorization") token: String,
        @Url url: String,
        @Body data: HashMap<String, Any>
    ): Response<JsonObject>

    @PUT
    suspend fun apiPutForRawBody(
        @Url url: String,
        @Header("Authorization") token: String,
        @Body data: HashMap<String, Any>,
    ): Response<JsonObject>


    @PUT
    suspend fun apiPutWithQuery(
        @Url url: String,
        @Header("Authorization") token: String,
        @QueryMap queryParams: Map<String, String>
    ): Response<JsonObject>

    @PUT
    suspend fun apiPutForRawBodyWithoutAuth(
        @Url url: String,
        @Body data: HashMap<String, Any>,
    ): Response<JsonObject>

    @PUT
    suspend fun apiPutWithoutRawBody(
        @Url url: String,
        @Header("Authorization") token: String,
    ): Response<JsonObject>


    @DELETE
    suspend fun deletePost(@Url url: String, @Header("Authorization") token: String) : Response<JsonObject>

    @FormUrlEncoded
    @POST
    suspend fun apiForFormData(
        @FieldMap data: HashMap<String, Any>, @Url url: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @PUT
    suspend fun apiForFormDataPut(
        @FieldMap data: HashMap<String, Any>,
        @Url url: String,
        @Header("Authorization") token: String
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST
    suspend fun apiForPostWithData(
        @FieldMap data: HashMap<String, Any>,
        @Url url: String,
        @Header("Authorization") token: String
    ): Response<JsonObject>


    @GET
    suspend fun apiGetOutWithQuery(@Url url: String): Response<JsonObject>

    @GET
    suspend fun apiGetOnlyAuthToken(
        @Url url: String, @Header("Authorization") token: String
    ): Response<JsonObject>


    @GET
    suspend fun apiGetWithQuery(
        @Url url: String, @QueryMap data: HashMap<String, String>
    ): Response<JsonObject>

    @GET
    suspend fun apiGetWithQueryAuth(
        @Url url: String, @QueryMap data: HashMap<String, Any>, @Header("Authorization") token: String
    ) :Response<JsonObject>

    @Multipart
    @JvmSuppressWildcards
    @POST
    suspend fun apiForPostMultipart(
        @Url url: String,
        @Header("Authorization") token: String,
        @PartMap data: Map<String, RequestBody>,
        @Part parts: MutableList<MultipartBody.Part>
    ): Response<JsonObject>

    @Multipart
    @JvmSuppressWildcards
    @POST
    suspend fun apiForPostMultipart(
        @Url url: String,
        @Header("Authorization") token: String,
        @PartMap data: Map<String, RequestBody>?,
        @Part parts: MultipartBody.Part?
    ): Response<JsonObject>


    @Headers(Constants.HEADER_API)
    @Multipart
    @JvmSuppressWildcards
    @PUT
    suspend fun apiForMultipartPut(
        @Url url: String,
        @Header("Authorization") token: String,
        @PartMap data: Map<String, RequestBody>?,
        @Part profilePic: MultipartBody.Part?,
    ): Response<JsonObject>


    @Multipart
    @JvmSuppressWildcards
    @PUT
    suspend fun apiForPutMultipart(
        @Url url: String,
        @Header("Authorization") token: String,
        @PartMap data: Map<String, RequestBody>?,
        @Part parts: MultipartBody.Part?,
        @Part partsList: MutableList<MultipartBody.Part>?
    ): Response<JsonObject>
    @Multipart
    @JvmSuppressWildcards
    @PUT
    suspend fun apiForPutMultipart1(
        @Url url: String,
        @Header("Authorization") token: String,
        @PartMap data: Map<String, RequestBody>?,
        @Part parts: MultipartBody.Part?,
        @Part partsList: MutableList<MultipartBody.Part?>?
    ): Response<JsonObject>




    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") token: String
    ): IndexQuote

}
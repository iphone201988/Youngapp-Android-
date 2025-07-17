package com.tech.young.data.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

interface ApiHelper {
    suspend fun apiForRawBody(request:HashMap<String, Any>,url: String): Response<JsonObject>
    suspend fun apiPostForRawBody(url: String, request:HashMap<String, Any>): Response<JsonObject>
    suspend fun apiForFormData(data: HashMap<String, Any>,url: String): Response<JsonObject>
    suspend fun apiForFormDataPut(data: HashMap<String, Any>,url: String): Response<JsonObject>
    suspend fun apiGetOutWithQuery(url:String): Response<JsonObject>
    suspend fun apiGetOnlyAuthToken(url:String): Response<JsonObject>
    suspend fun apiGetWithQuery(data: HashMap<String, String>,url: String): Response<JsonObject>
    suspend fun apiForPostMultipart(url: String,map: HashMap<String, RequestBody>, part: MutableList<MultipartBody.Part>): Response<JsonObject>
    suspend fun apiForPostMultipart(url: String,map: HashMap<String, RequestBody>?, part: MultipartBody.Part?): Response<JsonObject>
    suspend fun apiForMultipartPut(url: String,map: HashMap<String, RequestBody>?, part: MultipartBody.Part?): Response<JsonObject>
    suspend fun apiPutForRawBody(url: String,map: HashMap<String, Any>): Response<JsonObject>

    suspend fun apiGetWithQueryAuth(url: String, map: HashMap<String, Any>): Response<JsonObject>

    suspend fun apiPutWithoutRawBody(url: String):Response<JsonObject>

    suspend fun apiPutForRawBodyWithoutAuth(url: String, map: HashMap<String, Any>) :Response<JsonObject>

    suspend fun apiForPostWithData(url : String, data : HashMap<String,Any>) : Response<JsonObject>

    suspend fun apiForPutMultipart(url: String,map: HashMap<String, RequestBody>?, part: MultipartBody.Part?): Response<JsonObject>

    suspend fun apiPutWithQuery(url: String , data: HashMap<String, String>) : Response<JsonObject>

    suspend fun deletePost(url: String) : Response<JsonObject>
}


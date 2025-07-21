package com.tech.young.data.api

import com.tech.young.base.local.SharedPrefManager
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(private val apiService: ApiService, private val sharedPrefManager: SharedPrefManager) :
    ApiHelper {

    override suspend fun apiForRawBody(request: HashMap<String, Any>,url:String): Response<JsonObject> {
        return apiService.apiForRawBody(request,url)
    }

    override suspend fun apiPostForRawBody(
        url: String,
        request: HashMap<String, Any>,
    ): Response<JsonObject> {
        return apiService.apiPostForRawBody(getTokenFromSPref(), url, request)
    }

    override suspend fun apiForFormData(data: HashMap<String, Any>, url: String): Response<JsonObject> {
        return apiService.apiForFormData(data,url)
    }

    override suspend fun apiForFormDataPut(
        data: HashMap<String, Any>,
        url: String
    ): Response<JsonObject> {
        return apiService.apiForFormDataPut(data,url, getTokenFromSPref())
    }

    override suspend fun apiGetOutWithQuery(url:String): Response<JsonObject> {
        return apiService.apiGetOutWithQuery(url)
    }

    override suspend fun apiGetOnlyAuthToken(url: String): Response<JsonObject> {
        return apiService.apiGetOnlyAuthToken(url,getTokenFromSPref())
    }

    override suspend fun apiGetWithQuery(data: HashMap<String, String>, url: String): Response<JsonObject> {
        return apiService.apiGetWithQuery(url,data)
    }
    override suspend fun apiForPostMultipart(url: String,map: HashMap<String, RequestBody>,
                                          part: MutableList<MultipartBody.Part>): Response<JsonObject> {
        return apiService.apiForPostMultipart(url,getTokenFromSPref(), map, part)
    }

    override suspend fun apiForPostMultipart(
        url: String,
        map: HashMap<String, RequestBody>?,
        part: MultipartBody.Part?,
    ): Response<JsonObject> {
        return apiService.apiForPostMultipart(url,getTokenFromSPref(), map, part)
    }

    override suspend fun apiForMultipartPut(
        url: String,
        map: HashMap<String, RequestBody>?,
        part: MultipartBody.Part?
    ): Response<JsonObject> {
        return apiService.apiForMultipartPut(url,getTokenFromSPref(), map, part)
    }

    override suspend fun apiPutForRawBody(
        url: String,
        map: HashMap<String, Any>,
    ): Response<JsonObject> {
        return apiService.apiPutForRawBody(url,getTokenFromSPref(), map)
    }

    override suspend fun apiGetWithQueryAuth(
        url: String,
        map: HashMap<String, Any>
    ): Response<JsonObject> {
        return apiService.apiGetWithQueryAuth(url,map,getTokenFromSPref())
    }

    override suspend fun apiPutWithoutRawBody(url: String): Response<JsonObject> {
        return apiService.apiPutWithoutRawBody(url,getTokenFromSPref())
    }

    override suspend fun apiPutForRawBodyWithoutAuth(
        url: String,
        map: HashMap<String, Any>
    ): Response<JsonObject> {
        return apiService.apiPutForRawBodyWithoutAuth(url,map)
    }

    override suspend fun apiForPostWithData(
        url: String,
        data: HashMap<String, Any>
    ): Response<JsonObject> {
        return apiService.apiForPostWithData(data,url,getTokenFromSPref())
    }

    override suspend fun apiForPutMultipart(
        url: String,
        map: HashMap<String, RequestBody>?,
        part: MultipartBody.Part?,
        partList:MutableList<MultipartBody.Part>?
    ): Response<JsonObject> {
        return apiService.apiForPutMultipart(url,getTokenFromSPref(), map, part,partList)
    }

    override suspend fun apiPutWithQuery(
        url: String,
        data: HashMap<String, String>
    ): Response<JsonObject> {
        return apiService.apiPutWithQuery(url,getTokenFromSPref(),data)
    }

    override suspend fun deletePost(
        url: String
    ): Response<JsonObject> {
        return apiService.deletePost(url,getTokenFromSPref())
    }

    private fun getTokenFromSPref(): String {
        return "Bearer ${sharedPrefManager.getLoginData()?.token}"
    }
  /*  private fun getTokenFromSPref1(): String {
        return "Bearer ${sharedPrefManager.getCurrentUser()?.token}"
    }*/

}
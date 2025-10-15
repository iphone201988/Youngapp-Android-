package com.tech.young.ui.my_profile_screens

import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.Resource
import com.tech.young.base.utils.event.SingleRequestEvent
import com.tech.young.data.api.ApiHelper
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class YourProfileVM @Inject constructor(val apiHelper: ApiHelper):BaseViewModel() {

    val observeCommon = SingleRequestEvent<JsonObject>()


    fun getProfile(url: String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOnlyAuthToken(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getProfile", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }


    fun getEvents(data : HashMap<String,Any>, url: String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithQueryAuth(url,data)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getEvents", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun editEvent(request: HashMap<String, RequestBody>, url: String, profileImage: MultipartBody.Part?){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiForMultipartPut(url, request, profileImage)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("editEvent", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun addEvent(request: HashMap<String, RequestBody>, url: String, profileImage: MultipartBody.Part?){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiForPostMultipart(url, request, profileImage)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("addEvent", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun updateProfile(url: String, data: HashMap<String, RequestBody>, part: MultipartBody.Part?,partList:MutableList<MultipartBody.Part>?){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiForPutMultipart(url,data,part,partList)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("updateProfile", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun getAds(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOnlyAuthToken(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getAds", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun getRating(data : HashMap<String,Any> , url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithQueryAuth(url,data)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getRating", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }


    fun deleteEvent(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.deletePost(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("deleteEvent", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun logout(url: String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOnlyAuthToken(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("logout", response.body()))
                }
                else{
                    observeCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            }catch (e : Exception){
                observeCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }


    }
}
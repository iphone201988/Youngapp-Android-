package com.tech.young.ui.exchange

import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.Resource
import com.tech.young.base.utils.event.SingleRequestEvent
import com.tech.young.data.api.ApiHelper
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExchangeVM @Inject constructor(val apiHelper: ApiHelper): BaseViewModel() {

    val observeCommon = SingleRequestEvent<JsonObject>()
    fun getShare(data : HashMap<String,Any>, url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithQueryAuth(url,data)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getShare", response.body()))
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


    fun getVault(data : HashMap<String,Any>, url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithQueryAuth(url,data)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getVault", response.body()))
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

    fun getStream(data : HashMap<String,Any>, url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithQueryAuth(url,data)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getStream", response.body()))
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
    fun saveUnSave(data : HashMap<String,String>, url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPutWithQuery(url,data)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("saveUnSave", response.body()))
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


    fun deletePost( url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.deletePost(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("deletePost", response.body()))
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

    fun vaultSaveUnSave (url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPutWithoutRawBody(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("vaultSaveUnSave", response.body()))
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


    fun addComment(data : HashMap<String,Any>, url: String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPostForRawBody(url,data)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("addComment", response.body()))
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

    fun likeDislike(data : HashMap<String,String> , url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPutWithQuery(url, data)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("likeDislike", response.body()))
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

    fun reshare(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPutWithoutRawBody(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("reshare", response.body()))
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
    fun getShareDetail(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOnlyAuthToken(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getShareDetail", response.body()))
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

    fun getComments(url: String, data: HashMap<String, Any>){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithQueryAuth(url,data)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("getComments", response.body()))
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

    fun likeDislikeComment(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPutWithoutRawBody(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("likeDislikeComment", response.body()))
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

    fun rating(data : HashMap<String,Any>, url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPostForRawBody(url,data)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("rating", response.body()))
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


    fun scheduleSteam(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            observeCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiPutWithoutRawBody(url)
                if (response.isSuccessful && response.body() != null){
                    observeCommon.postValue(Resource.success("scheduleSteam", response.body()))
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